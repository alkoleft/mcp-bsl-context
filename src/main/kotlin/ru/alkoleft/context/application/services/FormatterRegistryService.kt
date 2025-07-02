/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.services

import org.springframework.stereotype.Service
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.FormattingContext
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter

/**
 * Application Service для управления форматтерами результатов
 * Реализует Strategy pattern для выбора подходящего форматтера
 */
@Service
class FormatterRegistryService(
    private val formatters: List<ResultFormatter>,
) {
    /**
     * Форматирование результатов поиска с использованием указанного формата
     */
    suspend fun formatSearchResults(
        result: SearchResult,
        formatType: FormatType = FormatType.MARKDOWN,
        context: FormattingContext = FormattingContext(),
    ): String {
        val formatter =
            getFormatter(formatType)
                ?: throw IllegalArgumentException("Formatter for type $formatType not found")

        return formatter.formatSearchResults(result)
    }

    /**
     * Форматирование детальной информации об элементе
     */
    suspend fun formatDetailedInfo(
        element: ApiElement,
        formatType: FormatType = FormatType.MARKDOWN,
        context: FormattingContext = FormattingContext(),
    ): String {
        val formatter =
            getFormatter(formatType)
                ?: throw IllegalArgumentException("Formatter for type $formatType not found")

        return formatter.formatDetailedInfo(element)
    }

    /**
     * Форматирование списка элементов
     */
    suspend fun formatElementList(
        elements: List<ApiElement>,
        title: String? = null,
        formatType: FormatType = FormatType.MARKDOWN,
        context: FormattingContext = FormattingContext(),
    ): String {
        val formatter =
            getFormatter(formatType)
                ?: throw IllegalArgumentException("Formatter for type $formatType not found")

        return formatter.formatElementList(elements, title)
    }

    /**
     * Получение MIME-type для указанного формата
     */
    fun getMimeType(formatType: FormatType): String {
        val formatter =
            getFormatter(formatType)
                ?: throw IllegalArgumentException("Formatter for type $formatType not found")

        return formatter.getMimeType()
    }

    /**
     * Получение списка поддерживаемых форматов
     */
    fun getSupportedFormats(): List<FormatType> = formatters.map { it.formatType }.distinct()

    /**
     * Проверка поддержки указанного формата
     */
    fun isFormatSupported(formatType: FormatType): Boolean = formatters.any { it.supports(formatType) }

    /**
     * Получение форматтера для указанного типа
     */
    private fun getFormatter(formatType: FormatType): ResultFormatter? = formatters.find { it.supports(formatType) }

    /**
     * Получение статистики по форматтерам
     */
    fun getFormatterStatistics(): FormatterStatistics =
        FormatterStatistics(
            totalFormatters = formatters.size,
            supportedFormats = getSupportedFormats(),
            formattersDetails =
                formatters.associate {
                    it.formatType.name to
                        FormatterInfo(
                            type = it.formatType,
                            mimeType = it.getMimeType(),
                        )
                },
        )
}

/**
 * Статистика форматтеров
 */
data class FormatterStatistics(
    val totalFormatters: Int,
    val supportedFormats: List<FormatType>,
    val formattersDetails: Map<String, FormatterInfo>,
)

/**
 * Информация о форматтере
 */
data class FormatterInfo(
    val type: FormatType,
    val mimeType: String,
)
