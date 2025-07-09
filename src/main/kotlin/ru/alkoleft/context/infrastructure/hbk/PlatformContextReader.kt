/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import io.github.oshai.kotlinlogging.KotlinLogging
import ru.alkoleft.context.infrastructure.hbk.HbkContentReader.Context
import ru.alkoleft.context.infrastructure.hbk.pages.GlobalContextPage
import ru.alkoleft.context.infrastructure.hbk.parsers.ConstructorInfo
import ru.alkoleft.context.infrastructure.hbk.parsers.EnumInfo
import ru.alkoleft.context.infrastructure.hbk.parsers.MethodInfo
import ru.alkoleft.context.infrastructure.hbk.parsers.ObjectInfo
import ru.alkoleft.context.infrastructure.hbk.parsers.PlatformContextPagesParser
import ru.alkoleft.context.infrastructure.hbk.parsers.PropertyInfo
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

private val CATALOG_PAGE_PATTERN = """/catalog\d+\.html""".toRegex()

/**
 * Основной класс для чтения контекста платформы 1С:Предприятие из HBK файлов.
 *
 * Этот класс координирует весь процесс извлечения и парсинга документации
 * из HBK файлов платформы 1С:Предприятие. Он обходит структуру страниц,
 * определяет их типы и делегирует парсинг специализированным парсерам.
 *
 * Основные возможности:
 * - Чтение и анализ структуры HBK файла
 * - Определение типов страниц (перечисления, объекты, глобальный контекст)
 * - Координация работы специализированных парсеров
 * - Сбор информации о перечислениях, объектах, методах и свойствах
 *
 * @see HbkContentReader для доступа к содержимому HBK файла
 * @see PlatformContextPagesParser для парсинга различных типов страниц
 */
class PlatformContextReader {
    private val enums = mutableListOf<EnumInfo>()
    private val objects = mutableListOf<ObjectInfo>()

    /**
     * Читает контекст платформы из HBK файла.
     *
     * @param path Путь к HBK файлу
     */
    fun read(path: Path) {
        val reader = HbkContentReader()
        reader.read(path) {
            visitPages(this, toc.pages)
        }
    }

    private fun visitPages(
        context: Context,
        pages: List<Page>,
    ) {
        val parser = PlatformContextPagesParser(context)

        pages
            .filter { it.htmlPath.isNotEmpty() }
            .forEach { page ->
                if (isGlobalContextPage(page)) {
                    visitGlobalContextPage(page, parser)
                } else if (isCatalogPage(page)) {
                    visitPages(context, page.children)
                } else if (isEnumPage(page)) {
                    visitEnumPage(page, parser)
                } else {
                    visitTypePage(page, parser)
                }
            }
    }

    /**
     * Обрабатывает страницу глобального контекста платформы.
     *
     * @param page Страница глобального контекста
     * @param parser Парсер для обработки страниц
     * @return Информация о глобальном контексте
     */
    private fun visitGlobalContextPage(
        page: Page,
        parser: PlatformContextPagesParser,
    ): GlobalContextPage {
        logger.info { "Анализ описания глобального контекста: ${page.title.ru}" }
        var properties: List<PropertyInfo> = emptyList()
        var methods = mutableListOf<MethodInfo>()
        page.children.forEach {
            when {
                it.title.en == "Свойства" ->
                    properties = getPropertiesFromPage(it, parser)
                it.htmlPath.contains("/methods/") ->
                    methods.addAll(getMethodsFromPage(it, parser))
            }
        }
        return GlobalContextPage(emptyList(), emptyList())
    }

    /**
     * Обрабатывает страницу перечисления.
     *
     * @param page Страница перечисления
     * @param parser Парсер для обработки страниц
     */
    private fun visitEnumPage(
        page: Page,
        parser: PlatformContextPagesParser,
    ) {
        val values =
            page.children
                .filter { it.htmlPath.contains("/properties/") }
                .map { parser.parseEnumValuePage(it) }
        enums += parser.parseEnumPage(page).apply { this.values.addAll(values) }
    }

    /**
     * Обрабатывает страницу типа (объекта).
     *
     * @param page Страница типа
     * @param parser Парсер для обработки страниц
     */
    private fun visitTypePage(
        page: Page,
        parser: PlatformContextPagesParser,
    ) {
        logger.debug { "Анализ описания типа: ${page.title.ru}" }
        val objectInfo = parser.parseObjectPage(page)
        var properties: List<PropertyInfo>
        var methods: List<MethodInfo>
        var constructors: List<ConstructorInfo>

        for (subPage in page.children) {
            when (subPage.title.en) {
                "Свойства" -> properties = getPropertiesFromPage(subPage, parser)
                "Методы" -> methods = getMethodsFromPage(subPage, parser)
                "Конструкторы" -> constructors = getConstructorsFromPage(subPage, parser)
            }
        }
        objects.add(objectInfo)
    }

    private fun getPropertiesFromPage(
        page: Page,
        parser: PlatformContextPagesParser,
    ) = page.children
        .filter { it.htmlPath.contains("/properties/") } // TODO проверить на обязательность
        .filter { !it.title.ru.startsWith("<") }
        .map { parser.parsePropertyPage(it) }

    private fun getMethodsFromPage(
        page: Page,
        parser: PlatformContextPagesParser,
    ) = page.children
        .map { parser.parseMethodPage(it) }

    private fun getConstructorsFromPage(
        page: Page,
        parser: PlatformContextPagesParser,
    ) = page.children
        .filter { it.htmlPath.contains("/ctors/") }
        .map { parser.parseConstructorPage(it) }

    private fun isGlobalContextPage(page: Page): Boolean = page.htmlPath.contains("Global context.html")

    private fun isCatalogPage(page: Page) = CATALOG_PAGE_PATTERN.find(page.htmlPath) != null

    private fun isEnumPage(page: Page): Boolean {
        // FIXME нужна проверка более точная
        return page.children.any { it.htmlPath.contains("/properties/") }
    }
}
