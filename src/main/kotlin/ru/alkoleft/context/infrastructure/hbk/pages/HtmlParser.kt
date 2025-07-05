/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.pages

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import java.io.InputStream
import java.util.regex.Pattern

class HtmlParser() {
    private val parameterNamePattern: Pattern = Pattern.compile("<(.*)>.*\\((.*)\\)")

    private fun parsePage(stream: InputStream, handler: KsoupHtmlHandler) {
        val parser = KsoupHtmlParser(handler = handler)
        stream.bufferedReader().use {
            it.forEachLine { line -> parser.write(line) }
        }
    }

    fun parsePropertyPage(stream: InputStream) {
        parsePage(stream, handler = {
            fun onOpenTag(name: String, attributes: Map<String, String>, isImplied: Boolean) {
                println("Open tag: $name")
            }
        } as KsoupHtmlHandler)
    }

}