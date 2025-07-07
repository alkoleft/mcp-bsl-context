/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

private val PARAMETER_NAME_PATTERN = """<([^&]+)>\s*(?:\(([^)]+)\))?""".toRegex()

/**
 * Базовый интерфейс для обработчиков блоков
 */
interface BlockHandler<R> : KsoupHtmlHandler {
    fun getResult(): R
}

abstract class BaseBlockHandler<R> : BlockHandler<R> {
    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {}
    override fun onCloseTag(name: String, isImplied: Boolean) {}
}

/**
 * Обработчик блока имени метода
 */
class NameBlockHandler : BlockHandler<Pair<String, String>> {
    private var nameRu = ""
    private var nameEn = ""
    private var inHeading = false

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        if (name == "p" && attributes["class"] == "V8SH_heading") {
            inHeading = true
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (inHeading && name == "p") {
            inHeading = false
        }
    }

    override fun onText(text: String) {
        if (inHeading) {
            val trimmed = text.trim()
            Helper.readName(trimmed)?.apply {
                nameRu = first
                nameEn = second
            }
        }
    }

    override fun getResult(): Pair<String, String> = Pair(nameRu, nameEn)
}

/**
 * Обработчик блока синтаксиса
 */
class SyntaxBlockHandler : BaseBlockHandler<String>() {
    private val syntax = StringBuilder()

    override fun onText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            syntax.append(text)
        }
    }

    override fun getResult(): String = syntax.toString().trim()
}

class ParametersBlockHandler : BlockHandler<List<MethodParameterInfo>> {
    private val parameters = mutableListOf<MethodParameterInfo>()
    private var inParameterRubric = false

    // Состояние текущего параметра
    private var currentParameterName = ""
    private var currentParameterNameBufer = StringBuilder()
    private var currentParameterOptional = false
    private val currentParameterType = StringBuilder()
    private val currentParameterDescription = StringBuilder()
    private var inParameterType = false
    private var blockType = BlockType.NONE
    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        when {
            name == "div" && attributes["class"] == "V8SH_rubric" -> {
                storeCurrentParameter()
                blockType = BlockType.NAME
            }
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (blockType == BlockType.NAME) {
            PARAMETER_NAME_PATTERN.find(currentParameterNameBufer.toString())?.let {
                currentParameterName = it.groupValues[1]
                currentParameterOptional = it.groupValues.getOrNull(2) == "необязательный"
            }
            blockType = BlockType.NONE
        }
        when (name) {
            "div" -> {
                if (inParameterRubric) {
                    storeCurrentParameter()
                    inParameterRubric = false
                }
            }
        }
    }

    override fun onText(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        when (blockType) {
            BlockType.NONE -> {
                if (trimmed == "Тип:") blockType = BlockType.TYPE
            }

            BlockType.NAME -> currentParameterNameBufer.append(trimmed)

            BlockType.TYPE -> if (trimmed != ".") {
                currentParameterType.append(trimmed)
            } else {
                blockType = BlockType.DESCRIPTION
            }

            BlockType.DESCRIPTION -> currentParameterDescription.append(trimmed)
        }
    }

    override fun getResult(): List<MethodParameterInfo> {
        storeCurrentParameter()
        return parameters.toList()
    }

    private fun storeCurrentParameter() {
        if (currentParameterName.isNotEmpty()) {
            parameters.add(
                MethodParameterInfo(
                    name = currentParameterName,
                    type = currentParameterType.toString().trim(),
                    isOptional = currentParameterOptional,
                    description = currentParameterDescription.toString().trim(),
                ),
            )
            currentParameterName = ""
            currentParameterOptional = false
            currentParameterType.clear()
            currentParameterDescription.clear()
            inParameterType = false
        }
    }

    enum class BlockType {
        NONE,
        NAME,
        TYPE,
        DESCRIPTION
    }
}

/**
 * Обработчик блока возвращаемого значения
 */
class ValueInfoBlockHandler : MarkdownHtmlHandler<ValueInfo?>() {
    private var returnValue: ValueInfo? = null

    // Состояние текущего возвращаемого значения
    private val currentValueType = StringBuilder()
    private var blockType = BlockType.NONE

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean
    ) {
        if (blockType == BlockType.DESCRIPTION) {
            super.onOpenTag(name, attributes, isImplied)
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (blockType == BlockType.DESCRIPTION) {
            super.onCloseTag(name, isImplied)
        }
    }

    override fun onText(text: String) {
        when (blockType) {
            BlockType.NONE -> {
                if (text.trim() == "Тип:") blockType = BlockType.TYPE
            }

            BlockType.TYPE -> {
                val trimmed = text.trim()
                if (trimmed != ".") {
                    currentValueType.append(trimmed)
                } else {
                    blockType = BlockType.DESCRIPTION
                }
            }

            BlockType.DESCRIPTION -> super.onText(text)
        }
    }

    override fun getResult(): ValueInfo? {
        if (returnValue == null && currentValueType.isNotEmpty()) {
            returnValue = ValueInfo(
                currentValueType.toString().trim(),
                getMarkdown()
            )
        }
        return returnValue
    }

    enum class BlockType {
        NONE,
        TYPE,
        DESCRIPTION
    }
}

/**
 * Обработчик блока описания
 */
class DescriptionBlockHandler() : MarkdownHtmlHandler<String>() {
    override fun getResult() = getMarkdown()
}

class NoteBlockHandler() : MarkdownHtmlHandler<String>() {
    override fun getResult() = getMarkdown()
}

/**
 * Обработчик блока описания
 */
class ExampleBlockHandler() : BaseBlockHandler<String>() {
    private val description = StringBuilder()

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean
    ) {
        if (name == "br" || name == "BR") {
            description.appendLine()
        }
    }

    override fun onText(text: String) {
        description.append(text)
    }

    override fun getResult(): String = description.toString().trim().replace("\u00a0", " ")
}

class RelatedObjectsBlockHandler() : BaseBlockHandler<List<RelatedObject>>() {
    private val relatedObjects = mutableListOf<RelatedObject>()
    private var linkText = StringBuilder()
    private var href: String? = ""
    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        linkText.clear()
        href = attributes["href"]
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (name == "a" && linkText.isNotEmpty()) {
            if (href != null) {
                val text = linkText.toString().trim()
                    .replace(" ,", ",")
                    .replace(Regex("\\s{2,}"), " ")

                relatedObjects += RelatedObject(text, href!!)
            } else {
                throw IllegalArgumentException("Link href is empty for `$linkText`")
            }
            href = null
            linkText.clear()
        }
    }

    override fun onText(text: String) {
        linkText.append(text)
    }

    override fun getResult(): List<RelatedObject> = relatedObjects
}

class ReadOnlyBlockHandler() : BaseBlockHandler<Boolean>() {
    private var value = false
    override fun onText(text: String) {
        value = text.startsWith("Только чтение")
    }

    override fun getResult() = value
}