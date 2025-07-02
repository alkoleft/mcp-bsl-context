/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.search

import org.springframework.stereotype.Component
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.MatchReason
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResultItem
import ru.alkoleft.context.core.ports.outgoing.SearchEngine
import ru.alkoleft.context.core.ports.outgoing.SearchEngineConfiguration
import kotlin.math.max
import kotlin.math.min

/**
 * Реализация нечеткого поиска с алгоритмом расстояния Левенштейна
 * Основан на существующем алгоритме из PlatformApiSearchService
 */
@Component
class FuzzySearchEngine : SearchEngine {
    override val algorithm = SearchAlgorithm.FUZZY

    companion object {
        private const val MIN_SIMILARITY_THRESHOLD = 0.3
        private const val EXACT_MATCH_BOOST = 0.3
        private const val PREFIX_MATCH_BOOST = 0.2
        private const val CONTAINS_MATCH_BOOST = 0.1
    }

    override suspend fun search(
        query: SearchQuery,
        candidates: List<ApiElement>,
    ): List<SearchResultItem> {
        val searchText = query.text.lowercase()
        val results = mutableListOf<SearchResultItem>()

        candidates.forEach { element ->
            val similarity = calculateSimilarity(searchText, element.name.lowercase())

            if (similarity >= MIN_SIMILARITY_THRESHOLD) {
                val matchReason = determineMatchReason(searchText, element.name.lowercase(), similarity)
                val boostedScore = applyBoosts(similarity, matchReason)

                results.add(
                    SearchResultItem(
                        element = element,
                        relevanceScore = boostedScore,
                        matchReason = matchReason,
                        highlightedText = highlightMatches(element.name, searchText),
                    ),
                )
            }
        }

        return results.sortedByDescending { it.relevanceScore }
    }

    override fun getConfiguration(): SearchEngineConfiguration =
        SearchEngineConfiguration(
            name = "Fuzzy Search Engine",
            version = "1.0.0",
            parameters =
                mapOf(
                    "algorithm" to "Levenshtein Distance",
                    "minSimilarityThreshold" to MIN_SIMILARITY_THRESHOLD,
                    "exactMatchBoost" to EXACT_MATCH_BOOST,
                    "prefixMatchBoost" to PREFIX_MATCH_BOOST,
                    "containsMatchBoost" to CONTAINS_MATCH_BOOST,
                ),
        )

    /**
     * Вычисление сходства с использованием расстояния Левенштейна
     */
    private fun calculateSimilarity(
        query: String,
        candidate: String,
    ): Double {
        if (query == candidate) return 1.0
        if (query.isEmpty() || candidate.isEmpty()) return 0.0

        val distance = levenshteinDistance(query, candidate)
        val maxLength = max(query.length, candidate.length)

        return 1.0 - (distance.toDouble() / maxLength.toDouble())
    }

    /**
     * Реализация алгоритма расстояния Левенштейна
     */
    private fun levenshteinDistance(
        s1: String,
        s2: String,
    ): Int {
        val len1 = s1.length
        val len2 = s2.length

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        // Инициализация базовых случаев
        for (i in 0..len1) {
            dp[i][0] = i
        }
        for (j in 0..len2) {
            dp[0][j] = j
        }

        // Заполнение матрицы динамического программирования
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1

                dp[i][j] =
                    min(
                        min(
                            dp[i - 1][j] + 1, // удаление
                            dp[i][j - 1] + 1, // вставка
                        ),
                        dp[i - 1][j - 1] + cost, // замена
                    )
            }
        }

        return dp[len1][len2]
    }

    /**
     * Определение причины совпадения
     */
    private fun determineMatchReason(
        query: String,
        candidate: String,
        similarity: Double,
    ): MatchReason =
        when {
            query == candidate -> MatchReason.ExactMatch("name")
            candidate.startsWith(query) -> MatchReason.PrefixMatch("name")
            candidate.contains(query) -> MatchReason.ContainsMatch("name")
            else -> MatchReason.FuzzyMatch("name", similarity)
        }

    /**
     * Применение бустов к базовому скору
     */
    private fun applyBoosts(
        baseSimilarity: Double,
        matchReason: MatchReason,
    ): Double {
        val boost =
            when (matchReason) {
                is MatchReason.ExactMatch -> EXACT_MATCH_BOOST
                is MatchReason.PrefixMatch -> PREFIX_MATCH_BOOST
                is MatchReason.ContainsMatch -> CONTAINS_MATCH_BOOST
                else -> 0.0
            }

        return min(1.0, baseSimilarity + boost)
    }

    /**
     * Подсветка совпадений в тексте
     */
    private fun highlightMatches(
        original: String,
        query: String,
    ): String? =
        if (original.lowercase().contains(query.lowercase())) {
            original.replace(query, "**$query**", ignoreCase = true)
        } else {
            null
        }
}
