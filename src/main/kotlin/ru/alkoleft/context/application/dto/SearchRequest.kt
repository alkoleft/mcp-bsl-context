/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.dto

import ru.alkoleft.context.core.ports.outgoing.FormatType

/**
 * DTO для поискового запроса на уровне приложения.
 * Используется для передачи данных между контроллерами и application services
 */
data class SearchRequest(
    // Основные параметры поиска
    val query: String,
    val algorithm: String = "INTELLIGENT",
    val elementTypes: List<String> = listOf("METHOD", "PROPERTY", "TYPE", "CONSTRUCTOR"),
    val limit: Int = 10,
    // Опции поиска
    val includeInherited: Boolean = false,
    val caseSensitive: Boolean = false,
    val exactMatch: Boolean = false,
    // Контекст поиска
    val parentType: String? = null,
    val language: String = "RU",
    val includeDeprecated: Boolean = false,
    val relevanceThreshold: Double = 0.1,
    // Опции форматирования
    val formatType: FormatType = FormatType.MARKDOWN,
    val includeMetadata: Boolean = true,
    val includeStatistics: Boolean = false,
    val maxLength: Int? = null,
    val highlightMatches: Boolean = true,
) {
    init {
        require(query.isNotBlank()) { "Query cannot be blank" }
        require(limit > 0) { "Limit must be positive" }
        require(limit <= 100) { "Limit cannot exceed 100" }
        require(relevanceThreshold in 0.0..1.0) { "Relevance threshold must be between 0.0 and 1.0" }
    }
}
