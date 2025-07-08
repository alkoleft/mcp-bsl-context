/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.rmi.UnexpectedException

class MethodPageProxyHandler : PageProxyHandler<MethodInfo>() {
    private var nameRu: String? = null
    private var nameEn: String? = null
    private val signatures = mutableListOf<MethodSignatureInfo>()
    private var example: String? = null
    private var relatedObjects: List<RelatedObject>? = null
    private var note: String?=null

    private val currentSignature: MethodSignatureInfo
        get() = signatures.last()

    override fun createHandler(blockTitle: String): BlockHandler<*>? =
        if (blockTitle.startsWith("Вариант синтаксиса:")) {
            handleNewSignature(blockTitle)
            null
        } else {
            when (blockTitle) {
                "Синтаксис:", "Вариант синтаксиса:" -> SyntaxBlockHandler()
                "Параметры:" -> ParametersBlockHandler()
                "Возвращаемое значение:" -> ValueInfoBlockHandler()
                "Описание:", "Описание варианта метода:" -> DescriptionBlockHandler()
                "Пример:" -> ExampleBlockHandler() // Placeholder, can be a specific handler
                "См. также:" -> RelatedObjectsBlockHandler() // Placeholder, can be a specific handler
                "Примечание:"->NoteBlockHandler()
                "Доступность:", "Использование в версии:" -> null
                else -> throw UnexpectedException("Неизвестный тип блока страницы описания `$blockTitle`")
            }
        }

    override fun onBlockFinished(handler: BlockHandler<*>) {
        when (handler) {
            is NameBlockHandler ->
                handler.getResult().apply {
                    nameRu = first
                    nameEn = second
                }

            is SyntaxBlockHandler -> {
                if (signatures.isEmpty()) {
                    signatures += MethodSignatureInfo("Основная", "", mutableListOf(), null, "")
                }
                currentSignature.syntax = handler.getResult()
            }

            is ParametersBlockHandler -> currentSignature.parameters = handler.getResult()
            is ValueInfoBlockHandler -> currentSignature.returnValue = handler.getResult()
            is DescriptionBlockHandler -> currentSignature.description = handler.getResult()
            is ExampleBlockHandler -> example = handler.getResult()
            is RelatedObjectsBlockHandler -> relatedObjects = handler.getResult()
            is NoteBlockHandler -> note = handler.getResult()
            else -> throw UnexpectedException("Не реализована обработка парсера `$handler`")
        }
    }

    override fun getResult(): MethodInfo {
        return MethodInfo(
            nameRu = nameRu as String,
            nameEn = nameEn as String,
            signatures = signatures,
            example = example,
            relatedObjects = relatedObjects,
            note = note
        )
    }

    override fun clean() {
        nameRu = null
        nameEn = null
        signatures.clear()
        example = null
        relatedObjects = null
        note = note
    }

    fun handleNewSignature(blockTitle: String) {
        signatures += MethodSignatureInfo(blockTitle.substring(19).trim(), "", mutableListOf(), null, "")
    }
}

/**
 * Новый парсер методов с архитектурой прокси-хэндлеров
 */
class MethodPageParser : PageParser<MethodInfo>(MethodPageProxyHandler())