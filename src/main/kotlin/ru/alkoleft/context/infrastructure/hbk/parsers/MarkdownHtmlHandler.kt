/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

typealias OpenTagHandler = (String, Map<String, String>, Boolean, MarkdownHtmlHandler) -> Unit
typealias CloseTagHandler = (String, Boolean, MarkdownHtmlHandler) -> Unit
typealias TextHandler = (String, MarkdownHtmlHandler) -> Unit

/**
 * Базовый обработчик HTML для конвертации в Markdown
 * Поддерживает расширение через кастомные обработчики
 */
class MarkdownHtmlHandler private constructor(
    private var isMarkdownEnabled: Boolean,
    private val customOpenTagHandler: OpenTagHandler? = null,
    private val customCloseTagHandler: CloseTagHandler? = null,
    private val customTextHandler: TextHandler? = null
) {
    private val markdownBuilder = StringBuilder()
    private val tagStack = mutableListOf<String>()
    private val tagAttributes = mutableMapOf<String, Map<String, String>>()
    private var isInList = false
    private var listLevel = 0
    private var isInCode = false
    private var isInPre = false
    private var isInBlockquote = false
    private var linkHref: String? = null
    private var linkText = StringBuilder()

    /**
     * Строитель для создания MarkdownHtmlHandler
     */
    class Builder {
        private var isMarkdownEnabled: Boolean = false
        private var onOpenTagHandler: OpenTagHandler? = null
        private var onCloseTagHandler: CloseTagHandler? = null
        private var onTextHandler: TextHandler? = null

        fun enableMarkdown(enable: Boolean): Builder {
            this.isMarkdownEnabled = enable
            return this
        }

        fun onOpenTag(handler: OpenTagHandler): Builder {
            this.onOpenTagHandler = handler
            return this
        }

        fun onCloseTag(handler: CloseTagHandler): Builder {
            this.onCloseTagHandler = handler
            return this
        }

        fun onText(handler: TextHandler): Builder {
            this.onTextHandler = handler
            return this
        }

        fun build(): MarkdownHtmlHandler {
            return MarkdownHtmlHandler(
                isMarkdownEnabled,
                onOpenTagHandler,
                onCloseTagHandler,
                onTextHandler
            )
        }
    }

    fun setMarkdownEnabled(enabled: Boolean) {
        isMarkdownEnabled = enabled
    }

    fun isMarkdownEnabled(): Boolean {
        return isMarkdownEnabled
    }

    val currentTagName: String
        get() = tagStack.last()

    val currentTagAttributes: Map<String, String>?
        get() = tagAttributes[currentTagName]

    private fun handleOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        tagStack.add(name)
        tagAttributes[name] = attributes

        if (isMarkdownEnabled && name == "p" && attributes["class"] == "V8SH_chapter") {
            isMarkdownEnabled = false
        }

        if (!isMarkdownEnabled) {
            customOpenTagHandler?.invoke(name, attributes, isImplied, this)
            return
        }
        // Если выключен режим Markdown, не выполняем стандартную обработку

        when (name.lowercase()) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                val level = name[1].digitToInt()
                markdownBuilder.append("\n${"#".repeat(level)} ")
            }

            "p" -> {
                // Пропускаем обработку для HBK-специфичных классов
                if (markdownBuilder.isNotEmpty()) {
                    markdownBuilder.append("\n")
                }
            }

            "br" -> {
                markdownBuilder.append("\n")
            }

            "strong", "b" -> {
                markdownBuilder.append("**")
            }

            "em", "i" -> {
                markdownBuilder.append("*")
            }

            "code" -> {
                if (!isInPre) {
                    markdownBuilder.append("`")
                    isInCode = true
                }
            }

            "pre" -> {
                markdownBuilder.append("\n```\n")
                isInPre = true
            }

            "blockquote" -> {
                markdownBuilder.append("\n> ")
                isInBlockquote = true
            }

            "ul" -> {
                isInList = true
                listLevel++
            }

            "ol" -> {
                isInList = true
                listLevel++
            }

            "li" -> {
                if (isInList) {
                    markdownBuilder.append("\n${"  ".repeat(listLevel - 1)}* ")
                }
            }

            "a" -> {
                linkHref = attributes["href"]
                linkText.clear()
            }
        }
    }

    private fun handleCloseTag(name: String, isImplied: Boolean) {
        // Если выключен режим Markdown, не выполняем стандартную обработку
        if (!isMarkdownEnabled) {
            // Выполняем кастомный обработчик если установлен
            customCloseTagHandler?.invoke(name, isImplied, this)
            afterCloseTag(name)
            return
        }

        when (name.lowercase()) {
            "h1", "h2", "h3", "h4", "h5", "h6" -> {
                markdownBuilder.append("\n")
            }

            "p" -> {
                markdownBuilder.append("\n")
            }

            "strong", "b" -> {
                markdownBuilder.append("**")
            }

            "em", "i" -> {
                markdownBuilder.append("*")
            }

            "code" -> {
                if (!isInPre) {
                    markdownBuilder.append("`")
                    isInCode = false
                }
            }

            "pre" -> {
                markdownBuilder.append("\n```\n")
                isInPre = false
            }

            "blockquote" -> {
                markdownBuilder.append("\n")
                isInBlockquote = false
            }

            "ul", "ol" -> {
                listLevel--
                if (listLevel == 0) {
                    isInList = false
                }
            }

            "table", "tr", "th", "td" -> {
                // Таблицы не поддерживаются
            }

            "a" -> {
                val text = linkText.toString().trim()
                val href = linkHref ?: ""
                if (text.isNotEmpty()) {
                    if(href.startsWith("v8help://")){
                        markdownBuilder.append(" `$text`")
                    }else {
                        markdownBuilder.append(" [$text]($href)")
                    }
                }
                linkHref = null
                linkText.clear()
            }
        }
        afterCloseTag(name)
    }

    private fun afterCloseTag(name: String) {
        if (tagStack.isNotEmpty() && tagStack.last() == name) {
            tagStack.removeLast()
            tagAttributes.remove(name)
        }
    }

    private fun handleText(text: String) {

        // Если выключен режим Markdown, не выполняем стандартную обработку
        if (!isMarkdownEnabled) {
            // Выполняем кастомный обработчик если установлен
            customTextHandler?.invoke(text, this)
            return
        }

        val trimmedText = text.trim()
        if (trimmedText.isNotEmpty()) {
            if (isInBlockquote) {
                markdownBuilder.append(trimmedText)
            } else if (isInPre) {
                markdownBuilder.append(text)
            } else if (tagStack.lastOrNull()?.lowercase() == "a") {
                linkText.append(text)
            } else {
                markdownBuilder.append(text)
            }
        }
    }

    fun getMarkdown(): String {
        return markdownBuilder.toString().trim()
    }

    /**
     * Создает KsoupHtmlHandler для использования с парсером
     */
    fun createKsoupHandler(): KsoupHtmlHandler {
        return KsoupHtmlHandler.Builder()
            .onOpenTag { name, attributes, isImplied ->
                handleOpenTag(name, attributes, isImplied)
            }
            .onCloseTag { name, isImplied ->
                handleCloseTag(name, isImplied)
            }
            .onText { text ->
                handleText(text)
            }
            .build()
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}