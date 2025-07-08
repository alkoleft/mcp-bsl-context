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

    fun cleanState()
}

abstract class BaseBlockHandler<R> : BlockHandler<R> {
    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
    }
}

/**
 * Обработчик блока имени метода
 */
class NameBlockHandler : BlockHandler<Pair<String, String>> {
    private var title = StringBuilder()
    private var heading = StringBuilder()
    private var inHeading = false
    private var inTitle = false

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        if (name == "p") {
            inHeading = (attributes["class"] == "V8SH_heading")
            inTitle = (attributes["class"] == "V8SH_title")
        }
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
        inHeading = false
    }

    override fun onText(text: String) {
        if (inHeading) {
            heading.append(text)
        } else if (inTitle) {
            title.append(text)
        }
    }

    override fun getResult(): Pair<String, String> = Helper.readName(if (heading.isNotEmpty()) heading.trim() else title.trim())!!

    override fun cleanState() {
        heading.clear()
        title.clear()
        inHeading = false
        inTitle = false
    }
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

    override fun cleanState() {
        syntax.clear()
    }
}

class ParametersBlockHandler : MarkdownHtmlHandler<List<MethodParameterInfo>>() {
    private val parameters = mutableListOf<MethodParameterInfo>()

    // Состояние текущего параметра
    private var currentParameterName = ""
    private var currentParameterNameBufer = StringBuilder()
    private var currentParameterOptional = false
    private val currentParameterType = StringBuilder()
    private var inParameterType = false
    private var blockType = BlockType.NONE

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        when {
            name == "div" && attributes["class"] == "V8SH_rubric" -> {
                storeCurrentParameter()
                blockType = BlockType.NAME
            }
            blockType == BlockType.DESCRIPTION -> super.onOpenTag(name, attributes, isImplied)
        }
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
        when (blockType) {
            BlockType.NAME -> {
                PARAMETER_NAME_PATTERN.find(currentParameterNameBufer.toString())?.let {
                    currentParameterName = it.groupValues[1]
                    currentParameterOptional = it.groupValues.getOrNull(2) == "необязательный"
                }
                blockType = BlockType.NONE
            }

            BlockType.DESCRIPTION -> super.onCloseTag(name, isImplied)
            else -> {}
        }
    }

    override fun onText(text: String) {
        when (blockType) {
            BlockType.NONE -> {
                if (text.trim() == "Тип:") blockType = BlockType.TYPE
            }

            BlockType.NAME -> currentParameterNameBufer.append(text.trim())

            BlockType.TYPE -> {
                val trimmed = text.trim()
                if (trimmed != ".") {
                    currentParameterType.append(trimmed)
                } else {
                    blockType = BlockType.DESCRIPTION
                }
            }

            BlockType.DESCRIPTION -> super.onText(text)
        }
    }

    override fun getResult(): List<MethodParameterInfo> {
        storeCurrentParameter()
        return parameters.toList()
    }

    override fun cleanState() {
        parameters.clear()
        cleanParameterState()
    }

    private fun storeCurrentParameter() {
        if (currentParameterName.isNotEmpty()) {
            parameters.add(
                MethodParameterInfo(
                    name = currentParameterName,
                    type = currentParameterType.toString().trim(),
                    isOptional = currentParameterOptional,
                    description = getMarkdown(),
                ),
            )
            cleanParameterState()
        }
    }

    private fun cleanParameterState() {
        currentParameterName = ""
        currentParameterNameBufer.clear()
        currentParameterOptional = false
        currentParameterType.clear()
        inParameterType = false
        blockType = BlockType.NONE
        super.cleanState()
    }

    enum class BlockType {
        NONE,
        NAME,
        TYPE,
        DESCRIPTION,
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
        isImplied: Boolean,
    ) {
        if (blockType == BlockType.DESCRIPTION) {
            super.onOpenTag(name, attributes, isImplied)
        }
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
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
            returnValue =
                ValueInfo(
                    currentValueType.toString().trim(),
                    getMarkdown(),
                )
        }
        return returnValue
    }

    override fun cleanState() {
        super.cleanState()
        returnValue = null
        currentValueType.clear()
        blockType = BlockType.NONE
    }

    enum class BlockType {
        NONE,
        TYPE,
        DESCRIPTION,
    }
}

/**
 * Обработчик блока описания
 */
class DescriptionBlockHandler : MarkdownHtmlHandler<String>() {
    override fun getResult() = getMarkdown()
}

class NoteBlockHandler : MarkdownHtmlHandler<String>() {
    override fun getResult() = getMarkdown()
}

/**
 * Обработчик блока описания
 */
class ExampleBlockHandler : BaseBlockHandler<String>() {
    private val description = StringBuilder()

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        if (name == "br" || name == "BR") {
            description.appendLine()
        }
    }

    override fun onText(text: String) {
        description.append(text)
    }

    override fun getResult(): String = description.toString().trim().replace("\u00a0", " ")

    override fun cleanState() {
        description.clear()
    }
}

class RelatedObjectsBlockHandler : BaseBlockHandler<List<RelatedObject>>() {
    private val relatedObjects = mutableListOf<RelatedObject>()
    private var linkText = StringBuilder()
    private var href: String? = ""

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        linkText.clear()
        href = attributes["href"]
    }

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
        if (name == "a" && linkText.isNotEmpty()) {
            if (href != null) {
                val text =
                    linkText
                        .toString()
                        .trim()
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

    override fun cleanState() {
        relatedObjects.clear()
        linkText.clear()
        href = null
    }
}

class ReadOnlyBlockHandler : BaseBlockHandler<Boolean>() {
    private var value = false

    override fun onText(text: String) {
        value = text.startsWith("Только чтение")
    }

    override fun getResult() = value

    override fun cleanState() {
        value = false
    }
}
