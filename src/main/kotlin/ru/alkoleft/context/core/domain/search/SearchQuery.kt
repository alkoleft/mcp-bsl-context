/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.domain.search

import ru.alkoleft.context.core.domain.api.DataSource

/**
 * Доменная модель поискового запроса.
 * Поддерживает различные алгоритмы поиска и источники данных
 */
data class SearchQuery(
    val text: String,
    val options: SearchOptions = SearchOptions(),
    val context: SearchContext = SearchContext(),
) {
    init {
        require(text.isNotBlank()) { "Search text cannot be blank" }
    }
}

/**
 * Опции поиска
 */
data class SearchOptions(
    val algorithm: SearchAlgorithm = SearchAlgorithm.INTELLIGENT,
    val elementTypes: Set<ApiElementType> = ApiElementType.entries.toSet(),
    val dataSources: Set<DataSource> = setOf(DataSource.BSL_CONTEXT),
    val limit: Int = 10,
    val includeInherited: Boolean = false,
    val caseSensitive: Boolean = false,
    val exactMatch: Boolean = false,
) {
    init {
        require(limit > 0) { "Limit must be positive" }
        require(limit <= 100) { "Limit cannot exceed 100" }
    }
}

/**
 * Контекст поиска для дополнительной информации
 */
data class SearchContext(
    val parentType: String? = null,
    val preferredLanguage: Language = Language.RU,
    val includeDeprecated: Boolean = false,
    val relevanceThreshold: Double = 0.1,
)

/**
 * Поддерживаемые алгоритмы поиска
 */
enum class SearchAlgorithm {
    FUZZY, // Текущий нечеткий поиск
    INTELLIGENT, // Улучшенный интеллектуальный поиск
    RAG, // Будущий RAG поиск
    FULL_TEXT, // Будущий полнотекстовый поиск
    SEMANTIC, // Будущий семантический поиск
    HYBRID, // Комбинированный поиск
}

enum class ApiElementType {
    METHOD,
    PROPERTY,
    TYPE,
    CONSTRUCTOR,
}

enum class Language {
    RU,
    EN,
}
