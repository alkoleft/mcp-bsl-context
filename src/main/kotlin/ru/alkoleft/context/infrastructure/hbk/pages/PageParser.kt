/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.pages

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import ru.alkoleft.context.infrastructure.hbk.parsers.PageProxyHandler
import java.io.InputStream

abstract class PageParser<T>(protected val handler: PageProxyHandler<T>) {
    fun parse(inputStream: InputStream): T {
        parsePage(inputStream, handler)
        return handler.getResult()
    }

    protected fun parsePage(inputStream: InputStream, handler: KsoupHtmlHandler) {
        val parser = KsoupHtmlParser(handler = handler)
        inputStream.use { stream ->
            stream.bufferedReader().use {
                it.forEachLine { line -> parser.write(line) }
            }
        }
    }
}