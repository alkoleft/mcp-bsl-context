/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import com.github._1c_syntax.bsl.context.api.ContextConstructor
import com.github._1c_syntax.bsl.context.api.ContextEvent
import com.github._1c_syntax.bsl.context.api.ContextMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import ru.alkoleft.context.infrastructure.hbk.HbkContentReader.Context
import ru.alkoleft.context.infrastructure.hbk.pages.GlobalContextPage
import ru.alkoleft.context.infrastructure.hbk.parsers.PlatformContextPagesParser
import ru.alkoleft.context.infrastructure.hbk.parsers.PropertyInfo
import java.nio.file.Path

private val logger = KotlinLogging.logger { }

class PlatformContextReader() {
    fun read(path: Path) {

        val reader = HbkContentReader()
        reader.read(path) {
            visitPages(this, toc.pages)
        }
    }

    private fun visitPages(context: Context, pages: List<Page>) {
        val parser = PlatformContextPagesParser(context)

        pages
            .filter { it.htmlPath.isNotEmpty() }
            .forEach { page ->
                if (isGlobalContextPage(page)) {
                    visitGlobalContextPage(page, parser)
                } else if (isCatalogPage(page)) {
                    visitPages(context, page.children)
                } else if (isEnumPage(page)) {
                    context.getEntryStream(page)?.readAllBytes()
                } else {
                    visitTypePage(page, parser)
                }
            }
    }

    fun visitGlobalContextPage(page: Page, parser: PlatformContextPagesParser): GlobalContextPage {
        logger.info { "Анализ описания глобального контекста: ${page.title.ru}" }
        page.children.forEach {
            when {
                it.title.en == "Свойства" -> getPropertiesFromPage(page, parser)
            }
        }
        return GlobalContextPage(emptyList(), emptyList())
    }

    fun visitTypePage(page: Page, parser: PlatformContextPagesParser) {
        logger.info { "Анализ описания типа: ${page.title.ru}" }
        var properties: List<PropertyInfo>
        var methods = mutableListOf<ContextMethod?>()
        var events = mutableListOf<ContextEvent?>()
        var constructors = mutableListOf<ContextConstructor?>()

        for (subPage in page.children) {
            when (subPage.title.en) {
                "Свойства" -> properties = getPropertiesFromPage(page, parser)
//                "Методы" -> methods = getMethodsFromPage(subPage)
//                "События" -> events = getEventsFromPage(subPage)
//                "Конструкторы" -> constructors = getConstructors(subPage)
            }
        }
    }

    private fun getPropertiesFromPage(page: Page, parser: PlatformContextPagesParser) =
        page.children
            .filter { it.htmlPath.contains("/properties/") }  // TODO проверить на обязательность
            .filter { !it.title.ru.startsWith("<") }
            .mapNotNull { parser.parsePropertyPage(it) }


    private fun isGlobalContextPage(page: Page): Boolean {
        return page.htmlPath.contains("Global context.html")
    }

    private fun isCatalogPage(page: Page): Boolean {
        val elements: Array<String?> = page.htmlPath.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val endElement: String = if (elements.size > 1) {
            elements[elements.size - 1]!!
        } else {
            elements[0]!!
        }

        return endElement.contains("catalog")
    }

    private fun isEnumPage(page: Page): Boolean {
        // FIXME нужна проверка более точная
        return page.children.stream()
            .anyMatch { it.htmlPath.contains("/properties/") }
    }

}