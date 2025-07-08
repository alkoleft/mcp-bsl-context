/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.rmi.UnexpectedException

class ConstructorPageProxyHandler : PageProxyHandler<ConstructorInfo>() {
    private var name = ""
    private var syntax = ""
    private var parameters = listOf<MethodParameterInfo>()
    private var description = ""
    private var example: String? = null
    private var relatedObjects: List<RelatedObject>? = null
    private var note: String? = null

    override fun createHandler(blockTitle: String): BlockHandler<*>? =
        when (blockTitle) {
            "Синтаксис:" -> SyntaxBlockHandler()
            "Параметры:" -> ParametersBlockHandler()
            "Описание:" -> DescriptionBlockHandler()
            "Пример:" -> ExampleBlockHandler()
            "См. также:" -> RelatedObjectsBlockHandler()
            "Примечание:" -> NoteBlockHandler()
            "Доступность:", "Использование в версии:" -> null
            else -> throw UnexpectedException("Неизвестный тип блока страницы описания конструктора `$blockTitle`")
        }

    override fun onBlockFinished(handler: BlockHandler<*>) {
        when (handler) {
            is NameBlockHandler ->
                name = handler.getResult().first

            is SyntaxBlockHandler -> syntax = handler.getResult()
            is ParametersBlockHandler -> parameters = handler.getResult()
            is DescriptionBlockHandler -> description = handler.getResult()
            is ExampleBlockHandler -> example = handler.getResult()
            is RelatedObjectsBlockHandler -> relatedObjects = handler.getResult()
            is NoteBlockHandler -> note = handler.getResult()
            else -> throw UnexpectedException("Не реализована обработка парсера `$handler`")
        }
    }

    override fun getResult(): ConstructorInfo =
        ConstructorInfo(
            name = name,
            syntax = syntax,
            parameters = parameters,
            description = description.trim(),
            example = example,
            note = note,
            relatedObjects = relatedObjects,
        )

    override fun clean() {
        name = ""
        syntax = ""
        parameters = listOf()
        description = ""
        example = null
        relatedObjects = null
        note = null
    }
}

class ConstructorPageParser : PageParser<ConstructorInfo>(ConstructorPageProxyHandler())
