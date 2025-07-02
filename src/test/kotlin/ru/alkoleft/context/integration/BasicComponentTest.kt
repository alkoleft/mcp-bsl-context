/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.integration

import org.junit.jupiter.api.Test
import ru.alkoleft.context.application.services.FormatterRegistryService
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter
import ru.alkoleft.context.infrastructure.adapters.outgoing.formatters.JsonFormatter
import ru.alkoleft.context.infrastructure.adapters.outgoing.formatters.McpMarkdownFormatter
import ru.alkoleft.context.infrastructure.adapters.outgoing.formatters.PlainTextFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Базовый тест компонентов без Spring контекста
 * Проверяет работу основных компонентов архитектуры
 */
class BasicComponentTest {
    /**
     * Проверка создания основных форматтеров
     */
    @Test
    fun `should create all formatters`() {
        val markdownFormatter = McpMarkdownFormatter()
        val jsonFormatter = JsonFormatter()
        val plainTextFormatter = PlainTextFormatter()

        assertNotNull(markdownFormatter)
        assertNotNull(jsonFormatter)
        assertNotNull(plainTextFormatter)

        assertEquals(FormatType.MARKDOWN, markdownFormatter.formatType)
        assertEquals(FormatType.JSON, jsonFormatter.formatType)
        assertEquals(FormatType.PLAIN_TEXT, plainTextFormatter.formatType)
    }

    /**
     * Проверка работы FormatterRegistryService
     */
    @Test
    fun `formatter registry should work correctly`() {
        val formatters =
            listOf<ResultFormatter>(
                McpMarkdownFormatter(),
                JsonFormatter(),
                PlainTextFormatter(),
            )

        val registryService = FormatterRegistryService(formatters)

        val supportedFormats = registryService.getSupportedFormats()
        assertTrue(supportedFormats.contains(FormatType.MARKDOWN))
        assertTrue(supportedFormats.contains(FormatType.JSON))
        assertTrue(supportedFormats.contains(FormatType.PLAIN_TEXT))

        val stats = registryService.getFormatterStatistics()
        assertEquals(3, stats.totalFormatters)
        assertTrue(stats.supportedFormats.isNotEmpty())
    }

    /**
     * Проверка базовой работы форматтеров
     */
    @Test
    fun `formatters should have correct properties`() {
        val markdownFormatter = McpMarkdownFormatter()
        val jsonFormatter = JsonFormatter()
        val plainTextFormatter = PlainTextFormatter()

        assertEquals(FormatType.MARKDOWN, markdownFormatter.formatType)
        assertEquals(FormatType.JSON, jsonFormatter.formatType)
        assertEquals(FormatType.PLAIN_TEXT, plainTextFormatter.formatType)

        assertTrue(markdownFormatter.supports(FormatType.MARKDOWN))
        assertTrue(jsonFormatter.supports(FormatType.JSON))
        assertTrue(plainTextFormatter.supports(FormatType.PLAIN_TEXT))

        assertTrue(markdownFormatter.getMimeType().isNotEmpty())
        assertTrue(jsonFormatter.getMimeType().isNotEmpty())
        assertTrue(plainTextFormatter.getMimeType().isNotEmpty())
    }
}
