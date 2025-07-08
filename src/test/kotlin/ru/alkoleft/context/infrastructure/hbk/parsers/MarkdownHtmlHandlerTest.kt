/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MarkdownHtmlHandlerTest {
    private fun parseHtmlToMarkdown(html: String): String {
        val markdownHandler =
            object : MarkdownHtmlHandler<String>() {
                override fun getResult() = getMarkdown()
            }
        val parser = KsoupHtmlParser(markdownHandler)
        parser.write(html)
        return markdownHandler.getResult()
    }

    @Test
    fun `test basic markdown conversion`() {
        val html =
            """
            <h1>Заголовок 1</h1>
            <p>Это <strong>жирный</strong> текст с <em>курсивом</em>.</p>
            <ul>
                <li>Элемент списка 1</li>
                <li>Элемент списка 2</li>
            </ul>
            """.trimIndent()

        val result = parseHtmlToMarkdown(html)

        val expected =
            """
            # Заголовок 1

            Это **жирный** текст с *курсивом*.

            * Элемент списка 1
            * Элемент списка 2
            """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun `test custom HBK rules`() {
        val html =
            """
            <p class="heading">Справочники (Catalogs)</p>
            <p class="chapter">Описание:</p>
            <p>Это описание свойства.</p>
            <ul>
                <li>Первый элемент</li>
                <li>Второй элемент</li>
            </ul>
            """.trimIndent()

        val result = parseHtmlToMarkdown(html)

        val expected =
            """
            Справочники (Catalogs)

            Описание:

            Это описание свойства.

            * Первый элемент
            * Второй элемент
            """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun `test custom link rules`() {
        val html =
            """
            <p>Ссылка: <a href="https://example.com">Пример</a></p>
            <p>См. также: <a href="#related1">Связанный объект 1</a>, <a href="#related2">Связанный объект 2</a></p>
            """.trimIndent()

        val result = parseHtmlToMarkdown(html)

        val expected =
            """
            Ссылка: [Пример](https://example.com)

            См. также: [Связанный объект 1](#related1), [Связанный объект 2](#related2)
            """.trimIndent()

        assertEquals(expected, result)
    }

    @Test
    fun `test multiple custom rules`() {
        val html =
            """
            <p class="heading">Справочники (Catalogs)</p>
            <p>Описание с <a href="#type">типом</a> и списком:</p>
            <ul>
                <li>Элемент 1</li>
                <li>Элемент 2</li>
            </ul>
            """.trimIndent()

        val result = parseHtmlToMarkdown(html)

        val expected =
            """
            Справочники (Catalogs)

            Описание с [типом](#type) и списком:

            * Элемент 1
            * Элемент 2
            """.trimIndent()

        assertEquals(expected, result)
    }
}
