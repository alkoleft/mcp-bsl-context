/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

import ru.alkoleft.context.infrastructure.hbk.models.Chunk
import ru.alkoleft.context.infrastructure.hbk.models.DoubleLanguageString
import ru.alkoleft.context.infrastructure.hbk.models.NameContainer
import ru.alkoleft.context.infrastructure.hbk.models.NameObject
import ru.alkoleft.context.infrastructure.hbk.models.Page

/**
 * Представляет оглавление (Table of Contents) HBK файла.
 *
 * Класс содержит иерархическую структуру страниц документации,
 * извлеченную из сжатого блока PackBlock HBK файла. Оглавление
 * используется для навигации по документации и определения
 * типов страниц.
 *
 * Основные возможности:
 * - Парсинг структуры оглавления из бинарных данных
 * - Предоставление доступа к иерархии страниц
 * - Извлечение заголовков на двух языках
 * - Получение путей к HTML файлам
 *
 * @see TocParser для парсинга бинарных данных оглавления
 * @see Page для представления отдельных страниц
 */
class Toc {
    private constructor(pages: List<Page>) {
        this.pages = pages
    }

    val pages: List<Page>

    companion object {
        /**
         * Парсит оглавление из сжатого блока данных.
         *
         * @param packBlock Сжатые данные оглавления
         * @return Объект оглавления с иерархией страниц
         */
        fun parse(packBlock: ByteArray): Toc {
            val parser = TocParser()
            val toc = Page(DoubleLanguageString("TOC", "TOC"), "")
            val pagesById = mutableMapOf(0 to toc)
            parser.parseContent(packBlock).forEach { chunk ->
                pagesById[chunk.id] =
                    Page(
                        title = chunk.title,
                        htmlPath = chunk.htmlPath,
                        children = mutableListOf(),
                    ).also { pagesById[chunk.parentId]?.children?.add(it) }
            }
            return Toc(toc.children.toList())
        }

        private fun getName(nameContext: NameObject): String = nameContext.name.replace("\"", "")

        /**
         * Получает заголовок чанка на двух языках.
         */
        val Chunk.title: DoubleLanguageString
            get() {
                val nameContainer: NameContainer = properties.nameContainer
                val namesContext: List<NameObject>? = nameContainer.nameObjects

                if (namesContext == null || namesContext.isEmpty()) {
                    return DoubleLanguageString("", "")
                } else if (namesContext.size == 1) {
                    val engName: NameObject = namesContext[0]
                    return DoubleLanguageString(getName(engName), "")
                } else {
                    val ruName: NameObject = namesContext[0]
                    val engName: NameObject = namesContext[1]
                    return DoubleLanguageString(getName(engName), getName(ruName))
                }
            }

        /**
         * Получает путь к HTML файлу чанка.
         */
        val Chunk.htmlPath: String
            get() = properties.htmlPath.replace("\"", "")
    }
}
