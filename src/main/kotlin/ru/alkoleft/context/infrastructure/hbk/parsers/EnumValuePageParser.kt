/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.rmi.UnexpectedException

class EnumValuePageParseHandler : PageProxyHandler<EnumValueInfo>() {
    private var nameRu = ""
    private var nameEn = ""
    private var description = ""
    private var relatedObjects: List<RelatedObject>? = null

    override fun clean() {
        nameRu = ""
        nameEn = ""
        description = ""
        relatedObjects = null
    }

    override fun createHandler(blockTitle: String): BlockHandler<*>? =
        when (blockTitle) {
            "Описание:" -> DescriptionBlockHandler()
            "См. также:" -> RelatedObjectsBlockHandler()
            "Доступность:", "Использование в версии:" -> null
            else -> throw UnexpectedException("Неизвестный тип блока страницы описания `$blockTitle`")
        }

    override fun onBlockFinished(handler: BlockHandler<*>) {
        when (handler) {
            is NameBlockHandler ->
                handler.getResult().apply {
                    nameRu = first
                    nameEn = second
                }

            is DescriptionBlockHandler -> description = handler.getResult()
            is RelatedObjectsBlockHandler -> relatedObjects = handler.getResult()
            else -> throw UnexpectedException("Не реализована обработка парсера `$handler`")
        }
    }

    override fun getResult() = EnumValueInfo(
        nameRu = nameRu,
        nameEn = nameEn,
        description = description,
        relatedObjects = relatedObjects
    )
}

class EnumValuePageParser : PageParser<EnumValueInfo>(EnumValuePageParseHandler())