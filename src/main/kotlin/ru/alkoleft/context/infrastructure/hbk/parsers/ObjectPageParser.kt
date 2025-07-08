/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.rmi.UnexpectedException

class ObjectPageProxyHandler : PageProxyHandler<ObjectInfo>() {
    private var nameRu = ""
    private var nameEn = ""
    private var description = ""
    private var example: String? = null
    private var note: String? = null
    private var relatedObjects: List<RelatedObject>? = null

    override fun clean() {
        nameRu = ""
        nameEn = ""
        description = ""
        example = null
        note = null
        relatedObjects = null
    }

    override fun createHandler(blockTitle: String): BlockHandler<*>? =
        when (blockTitle) {
            "Описание:" -> DescriptionBlockHandler()
            "Пример:" -> ExampleBlockHandler()
            "См. также:" -> RelatedObjectsBlockHandler()
            "Примечание:" -> NoteBlockHandler()
            "Свойства:", "Методы:", "События:", "Конструкторы:", "Доступность:", "Использование в версии:" -> null // Игнорируем эти блоки
            else -> null // Игнорируем неизвестные блоки
        }

    override fun onBlockFinished(handler: BlockHandler<*>) {
        when (handler) {
            is NameBlockHandler ->
                handler.getResult().apply {
                    nameRu = first
                    nameEn = second
                }

            is DescriptionBlockHandler -> description = handler.getResult()
            is ExampleBlockHandler -> example = handler.getResult()
            is RelatedObjectsBlockHandler -> relatedObjects = handler.getResult()
            is NoteBlockHandler -> note = handler.getResult()
            else -> throw UnexpectedException("Не реализована обработка парсера `$handler`")
        }
    }

    override fun getResult(): ObjectInfo =
        ObjectInfo(
            nameRu = nameRu,
            nameEn = nameEn,
            description = description,
            example = example,
            note = note,
            relatedObjects = relatedObjects,
        )
}

/**
 * Парсер для страниц объектов
 */
class ObjectPageParser : PageParser<ObjectInfo>(ObjectPageProxyHandler())
