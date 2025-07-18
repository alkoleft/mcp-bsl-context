/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BlockHandlerTest {
    private lateinit var nameHandler: NameBlockHandler
    private lateinit var syntaxHandler: SyntaxBlockHandler
    private lateinit var parametersHandler: ParametersBlockHandler
    private lateinit var descriptionHandler: DescriptionBlockHandler

    @BeforeEach
    fun setUp() {
        nameHandler = NameBlockHandler()
        syntaxHandler = SyntaxBlockHandler()
        parametersHandler = ParametersBlockHandler()
        descriptionHandler = DescriptionBlockHandler()
    }

    @Test
    fun `test NameBlockHandler parses name with English translation`() {
        nameHandler.onOpenTag("p", mapOf("class" to "V8SH_heading"), false)
        nameHandler.onText("Массив (Array)")
        nameHandler.onCloseTag("p", false)

        val result = nameHandler.getResult()
        assertEquals("Массив", result.first)
        assertEquals("Array", result.second)
    }

    @Test
    fun `test NameBlockHandler parses name without English translation`() {
        nameHandler.onOpenTag("p", mapOf("class" to "V8SH_title"), false)
        nameHandler.onText("Массив")
        nameHandler.onCloseTag("p", false)

        val result = nameHandler.getResult()
        assertEquals("Массив", result.first)
        assertEquals("", result.second)
    }

    @Test
    fun `test NameBlockHandler readName method with parentheses`() {
        val result = nameHandler.readName("Массив (Array)")
        assertEquals("Массив", result.first)
        assertEquals("Array", result.second)
    }

    @Test
    fun `test NameBlockHandler readName method without parentheses`() {
        val result = nameHandler.readName("Массив")
        assertEquals("Массив", result.first)
        assertEquals("", result.second)
    }

    @Test
    fun `test SyntaxBlockHandler accumulates syntax text`() {
        syntaxHandler.onText("Новый Массив(")
        syntaxHandler.onText("<Количество>)")
        syntaxHandler.onText("")

        val result = syntaxHandler.getResult()
        assertEquals("Новый Массив(<Количество>)", result)
    }

    @Test
    fun `test SyntaxBlockHandler trims whitespace`() {
        syntaxHandler.onText("  Новый Массив()  ")

        val result = syntaxHandler.getResult()
        assertEquals("Новый Массив()", result)
    }

    @Test
    fun `test ParametersBlockHandler parses single parameter`() {
        // Имитируем HTML структуру параметра
        parametersHandler.onOpenTag("div", mapOf("class" to "V8SH_rubric"), false)
        parametersHandler.onText("<Количество> (необязательный)")
        parametersHandler.onCloseTag("div", false)

        parametersHandler.onText("Тип: ")

        parametersHandler.onOpenTag("a", mapOf("href" to "index.html"), false)
        parametersHandler.onText("Произвольный")
        parametersHandler.onCloseTag("a", false)
        parametersHandler.onText(".")

        parametersHandler.onOpenTag("br", emptyMap(), true)
        parametersHandler.onText("Описание параметра")

        val result = parametersHandler.getResult()
        assertEquals(1, result.size)

        val parameter = result[0]
        assertEquals("Количество", parameter.name)
        assertEquals("Произвольный", parameter.type)
        assertTrue(parameter.isOptional)
        assertEquals("Описание параметра", parameter.description)
    }

    @Test
    fun `test ParametersBlockHandler parses parameter without linked type`() {
        // Имитируем HTML структуру параметра
        parametersHandler.onOpenTag("div", mapOf("class" to "V8SH_rubric"), false)
        parametersHandler.onText("<Количество> (необязательный)")
        parametersHandler.onCloseTag("div", false)

        parametersHandler.onText("Тип: Произвольный.")

        parametersHandler.onOpenTag("br", emptyMap(), true)
        parametersHandler.onText("Описание параметра")

        val result = parametersHandler.getResult()
        assertEquals(1, result.size)

        val parameter = result[0]
        assertEquals("Количество", parameter.name)
        assertEquals("Произвольный", parameter.type)
        assertTrue(parameter.isOptional)
        assertEquals("Описание параметра", parameter.description)
    }

    @Test
    fun `test DescriptionBlockHandler accumulates description text`() {
        descriptionHandler.onText("Это описание ")
        descriptionHandler.onText("метода или свойства")
        descriptionHandler.onText(".")

        val result = descriptionHandler.getResult()
        assertEquals("Это описание метода или свойства.", result)
    }

    @Test
    fun `test DescriptionBlockHandler handles markdown formatting`() {
        descriptionHandler.onOpenTag("code", emptyMap(), false)
        descriptionHandler.onText("код")
        descriptionHandler.onCloseTag("code", false)
        descriptionHandler.onText(" в описании")

        val result = descriptionHandler.getResult()
        assertEquals("`код` в описании", result)
    }
}
