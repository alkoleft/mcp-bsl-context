/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.rmi.UnexpectedException

class PropertyPageProxyHandler : PageProxyHandler<PropertyInfo>() {
    private var nameRu = ""
    private var nameEn = ""
    private var description = ""
    private var readonly = false
    private var typeNames = ""
    private var relatedObjects: List<RelatedObject>? = null
    private var note: String? = null

    override fun createHandler(blockTitle: String): BlockHandler<*>? =
        when (blockTitle) {
            "Описание:" -> ValueInfoBlockHandler()
            "Использование:" -> ReadOnlyBlockHandler()
            "См. также:" -> RelatedObjectsBlockHandler()
            "Примечание:" -> NoteBlockHandler()
            "Доступность:" -> null
            "Использование в версии:" -> null
            else -> throw UnexpectedException("Неизвестный тип блока страницы описания `$blockTitle`")
        }

    override fun onBlockFinished(handler: BlockHandler<*>) {
        when (handler) {
            is NameBlockHandler ->
                handler.getResult().apply {
                    nameRu = first
                    nameEn = second
                }

            is ValueInfoBlockHandler -> handler.getResult()?.let { info ->
                typeNames = info.type
                description = info.description
            }

            is ReadOnlyBlockHandler -> readonly = handler.getResult()
            is RelatedObjectsBlockHandler -> relatedObjects = handler.getResult()
            is NoteBlockHandler -> note = handler.getResult()
            else -> throw UnexpectedException("Не реализована обработка парсера `$handler`")
        }
    }

    override fun getResult(): PropertyInfo = PropertyInfo(
        propertyNameRu = nameRu,
        propertyNameEn = nameEn,
        description = description.trim(),
        readonly = readonly,
        typeName = typeNames,
        relatedObjects = relatedObjects,
        note = note,
    )

    override fun clean() {
        nameRu = ""
        nameEn = ""
        description = ""
        readonly = false
        typeNames = ""
        relatedObjects = null
        note = null
    }
}

class PropertyPageParser : PageParser<PropertyInfo>(PropertyPageProxyHandler())