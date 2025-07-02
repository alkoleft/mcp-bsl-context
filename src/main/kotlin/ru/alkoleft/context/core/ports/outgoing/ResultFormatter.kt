/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.ports.outgoing

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchResult

/**
 * Исходящий порт для форматирования результатов
 * Реализует Strategy pattern для различных форматов вывода
 */
interface ResultFormatter {
    /**
     * Тип формата, который поддерживает этот форматтер
     */
    val formatType: FormatType

    /**
     * Форматирование результатов поиска
     */
    suspend fun formatSearchResults(result: SearchResult): String

    /**
     * Форматирование детальной информации об элементе
     */
    suspend fun formatDetailedInfo(element: ApiElement): String

    /**
     * Форматирование списка элементов
     */
    suspend fun formatElementList(
        elements: List<ApiElement>,
        title: String? = null,
    ): String

    /**
     * Проверка поддержки формата
     */
    fun supports(formatType: FormatType): Boolean = this.formatType == formatType

    /**
     * Получение MIME-type для данного формата
     */
    fun getMimeType(): String
}

/**
 * Поддерживаемые типы форматов
 */
enum class FormatType {
    MARKDOWN, // Markdown для MCP (текущий основной)
    JSON, // JSON для API интеграций
    PLAIN_TEXT, // Простой текст
    HTML, // HTML для веб-интерфейсов
    XML, // XML для специфических интеграций
}

/**
 * Контекст форматирования для дополнительных опций
 */
data class FormattingContext(
    val includeMetadata: Boolean = true,
    val includeStatistics: Boolean = false,
    val maxLength: Int? = null,
    val language: String = "ru",
    val highlightMatches: Boolean = true,
)
