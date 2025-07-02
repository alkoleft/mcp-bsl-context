/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.domain.search

import ru.alkoleft.context.core.domain.api.ApiElement

/**
 * Результат поиска с информацией о релевантности
 */
data class SearchResult(
    val query: SearchQuery,
    val items: List<SearchResultItem>,
    val totalCount: Int,
    val executionTimeMs: Long,
    val algorithm: SearchAlgorithm,
) {
    val hasResults: Boolean get() = items.isNotEmpty()
    val limitReached: Boolean get() = items.size >= query.options.limit
}

/**
 * Элемент результата поиска
 */
data class SearchResultItem(
    val element: ApiElement,
    val relevanceScore: Double,
    val matchReason: MatchReason,
    val highlightedText: String? = null,
) {
    init {
        require(relevanceScore in 0.0..1.0) { "Relevance score must be between 0.0 and 1.0" }
    }
}

/**
 * Причина совпадения для объяснения почему элемент найден
 */
sealed class MatchReason {
    data class ExactMatch(
        val field: String,
    ) : MatchReason()

    data class FuzzyMatch(
        val field: String,
        val similarity: Double,
    ) : MatchReason()

    data class PrefixMatch(
        val field: String,
    ) : MatchReason()

    data class ContainsMatch(
        val field: String,
    ) : MatchReason()

    data class SemanticMatch(
        val similarity: Double,
    ) : MatchReason()

    data class MultipleMatches(
        val reasons: List<MatchReason>,
    ) : MatchReason()
}

/**
 * Статистика поиска для анализа эффективности
 */
data class SearchStatistics(
    val totalElements: Int,
    val searchedElements: Int,
    val matchedElements: Int,
    val algorithmUsed: SearchAlgorithm,
    val cacheHit: Boolean = false,
)
