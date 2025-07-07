/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler

/**
 * Прокси-обработчик для переключения между блоками
 */
abstract class PageProxyHandler<R>(
    private var currentHandler: BlockHandler<*>? = NameBlockHandler()
) : KsoupHtmlHandler {
    private var isChapter = false

    abstract fun clean()

    abstract fun createHandler(blockTitle: String): BlockHandler<*>?

    override fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
        // Проверяем, не является ли это новым блоком
        if (name == "p" && attributes["class"] == "V8SH_chapter") {
            currentHandler?.apply(::onBlockFinished)
            currentHandler = null
            isChapter = true
        } else {
            currentHandler?.onOpenTag(name, attributes, isImplied)
        }
    }

    override fun onCloseTag(name: String, isImplied: Boolean) {
        if (!isChapter) {
            currentHandler?.onCloseTag(name, isImplied)
        } else {
            isChapter = false
        }
    }

    override fun onText(text: String) {
        if (isChapter) {
            currentHandler = createHandler(text.trim())
        } else {
            currentHandler?.onText(text)
        }
    }

    abstract fun onBlockFinished(handler: BlockHandler<*>)
    abstract fun getResult(): R
}