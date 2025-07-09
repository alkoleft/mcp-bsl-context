/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers.core

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

private const val DEFAULT_HANDLER_KEY = "DEFAULT"

/**
 * Прокси-обработчик для переключения между блоками HTML документации.
 *
 * Этот абстрактный класс предоставляет механизм для динамического переключения
 * между различными обработчиками блоков в зависимости от содержимого HTML.
 * Он анализирует структуру документа и делегирует обработку соответствующим
 * специализированным обработчикам.
 *
 * Основные возможности:
 * - Динамическое переключение между обработчиками блоков
 * - Управление состоянием обработчиков
 * - Обработка глав (chapters) в документации
 * - Кэширование созданных обработчиков
 *
 * @param R Тип результата обработки страницы
 * @param defaultHandler Обработчик по умолчанию
 *
 * @see BlockHandler для создания специализированных обработчиков
 * @see KsoupHtmlHandler для базовой функциональности обработки HTML
 */
abstract class PageProxyHandler<R>(
    private var defaultHandler: BlockHandler<*>? = NameBlockHandler(),
) : KsoupHtmlHandler {
    private var currentHandler: BlockHandler<*>? = null
    private val handlers = mutableMapOf(DEFAULT_HANDLER_KEY to defaultHandler)
    private var isChapter = false

    /**
     * Очищает состояние всех обработчиков.
     */
    fun cleanState() {
        currentHandler = getHandler(DEFAULT_HANDLER_KEY)
        handlers.values.forEach { it?.cleanState() }
        clean()
    }

    /**
     * Вызывается по завершении парсинга страницы.
     */
    fun onParsingFinished() {
        if (currentHandler != null) {
            onBlockFinished(currentHandler!!)
        }
    }

    /**
     * Очищает состояние прокси-обработчика.
     */
    protected abstract fun clean()

    /**
     * Получает или создает обработчик для указанного блока.
     *
     * @param blockTitle Название блока
     * @return Обработчик блока или null
     */
    private fun getHandler(blockTitle: String) =
        if (handlers.containsKey(blockTitle)) {
            handlers[blockTitle]?.also { it.cleanState() }
        } else {
            createHandler(blockTitle)?.also { handlers[blockTitle] = it }
        }

    /**
     * Создает обработчик для указанного блока.
     *
     * @param blockTitle Название блока
     * @return Новый обработчик или null, если обработчик не нужен
     */
    protected abstract fun createHandler(blockTitle: String): BlockHandler<*>?

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        // Проверяем, не является ли это новым блоком
        if ((name == "p" && attributes["class"] == "V8SH_chapter") || name == "hr") {
            currentHandler?.apply(::onBlockFinished)
            currentHandler = null
            isChapter = true
        } else {
            currentHandler?.onOpenTag(name, attributes, isImplied)
        }
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
        if (!isChapter) {
            currentHandler?.onCloseTag(name, isImplied)
        } else {
            isChapter = false
        }
    }

    override fun onText(text: String) {
        if (isChapter) {
            currentHandler = getHandler(text.trim())
            onBlockStarted(text, currentHandler)
        } else {
            currentHandler?.onText(text)
        }
    }

    open fun onBlockStarted(text: String, handler: BlockHandler<*>?) {}
    /**
     * Вызывается при завершении обработки блока.
     *
     * @param handler Завершивший работу обработчик
     */
    abstract fun onBlockFinished(handler: BlockHandler<*>)

    /**
     * Получает результат обработки страницы.
     *
     * @return Результат обработки
     */
    abstract fun getResult(): R
}
