/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.ObjectPageParser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ObjectPageParserTest {
    private val parser = ObjectPageParser()

    @Test
    fun `should parse COMSafeArray object correctly`() {
        // Arrange
        val html = javaClass.getResourceAsStream("/objects/COMSafeArray.html")!!

        // Act
        val result =
            assertDoesNotThrow {
                parser.parse(html)
            }

        // Assert
        assertEquals("COMSafeArray", result.nameRu)
        assertEquals("COMSafeArray", result.nameEn)
        assertTrue(result.description.isNotEmpty())
        assertTrue(result.description.contains("Объектная оболочка над многомерным массивом SAFEARRAY"))

        // Проверяем пример
        assertNotNull(result.example)
        assertTrue(result.example.contains("COMSafeArray"))

        // Проверяем связанные объекты
        assertNotNull(result.relatedObjects)
        assertTrue(result.relatedObjects.isNotEmpty())
    }

    @Test
    fun `should parse CallbackDescription object correctly`() {
        // Arrange
        val html = javaClass.getResourceAsStream("/objects/CallbackDescription.html")!!

        // Act
        val result =
            assertDoesNotThrow {
                parser.parse(html)
            }

        // Assert
        assertEquals("ОписаниеОповещения", result.nameRu)
        assertEquals("CallbackDescription", result.nameEn)
        assertTrue(result.description.isNotEmpty())
        assertTrue(result.description.contains("Используется для описания вызова процедуры"))

        // Проверяем пример
        assertNotNull(result.example)
        assertTrue(result.example.contains("ОписаниеОповещения"))

        // Проверяем связанные объекты
        assertNotNull(result.relatedObjects)
        assertTrue(result.relatedObjects.isNotEmpty())
    }

    @Test
    fun `should parse complex object correctly`() {
        // Arrange
        val html = javaClass.getResourceAsStream("/objects/object130.html")!!

        // Act
        val result =
            assertDoesNotThrow {
                parser.parse(html)
            }

        // Assert
        assertEquals("СправочникОбъект.<Имя справочника>", result.nameRu)
        assertEquals("CatalogObject.<Catalog name>", result.nameEn)
        assertTrue(result.description.isNotEmpty())
        assertTrue(result.description.contains("Предназначен для чтения, изменения, добавления и удаления элементов"))

        // Проверяем связанные объекты
        assertNotNull(result.relatedObjects)
        assertTrue(result.relatedObjects.isNotEmpty())
    }
}
