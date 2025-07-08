/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

/**
 * Базовый обработчик HTML для конвертации в Markdown
 * Поддерживает расширение через кастомные обработчики
 */
abstract class MarkdownHtmlHandler<R> : BlockHandler<R> {
    protected val markdownBuilder = StringBuilder()
    private val tagStack = mutableListOf<String>()
    private var isInList = false
    private var listLevel = 0
    private var isInCode = false
    private var isInPre = false
    private var isInBlockquote = false
    private var linkHref: String? = null
    private var linkText = StringBuilder()

    override fun onOpenTag(
        name: String,
        attributes: Map<String, String>,
        isImplied: Boolean,
    ) {
        tagStack.add(name)
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

    override fun onCloseTag(
        name: String,
        isImplied: Boolean,
    ) {
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
                    if (href.startsWith("v8help://")) {
                        markdownBuilder.append("`$text`")
                    } else {
                        markdownBuilder.append("[$text]($href)")
                    }
                }
                linkHref = null
                linkText.clear()
            }
        }
        afterCloseTag(name)
    }

    fun afterCloseTag(name: String) {
        if (tagStack.isNotEmpty() && tagStack.last() == name) {
            tagStack.removeLast()
        }
    }

    override fun onText(text: String) {
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

    fun getMarkdown() = markdownBuilder.toString().trim()

    override fun cleanState() {
        markdownBuilder.clear()
        tagStack.clear()
        isInList = false
        listLevel = 0
        isInCode = false
        isInPre = false
        isInBlockquote = false
        linkHref = null
        linkText.clear()
    }
}
