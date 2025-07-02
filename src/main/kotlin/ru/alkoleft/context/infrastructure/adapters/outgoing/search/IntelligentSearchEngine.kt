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
 * Улучшенный интеллектуальный поиск с множественными стратегиями сопоставления
 * Комбинирует точный поиск, поиск по префиксу, fuzzy search и семантические улучшения
 */
@Component
class IntelligentSearchEngine : SearchEngine {
    override val algorithm = SearchAlgorithm.INTELLIGENT

    companion object {
        // Веса для различных типов совпадений
        private const val EXACT_MATCH_WEIGHT = 1.0
        private const val PREFIX_MATCH_WEIGHT = 0.9
        private const val CONTAINS_MATCH_WEIGHT = 0.7
        private const val FUZZY_MATCH_WEIGHT = 0.6
        private const val CAMEL_CASE_MATCH_WEIGHT = 0.8
        private const val ACRONYM_MATCH_WEIGHT = 0.75

        // Пороги релевантности
        private const val MIN_RELEVANCE_THRESHOLD = 0.1
        private const val FUZZY_SIMILARITY_THRESHOLD = 0.4

        // Бонусы за специфичные особенности 1С
        private const val BSL_SPECIFIC_BONUS = 0.1
        private const val TYPE_NAME_BONUS = 0.05
    }

    override suspend fun search(
        query: SearchQuery,
        candidates: List<ApiElement>,
    ): List<SearchResultItem> {
        val searchText = query.text.trim()
        val results = mutableListOf<SearchResultItem>()

        candidates.forEach { element ->
            val matches = findAllMatches(searchText, element)
            val bestMatch = selectBestMatch(matches)

            if (bestMatch != null && bestMatch.score >= MIN_RELEVANCE_THRESHOLD) {
                val enhancedScore = enhanceScore(bestMatch.score, element, query)

                results.add(
                    SearchResultItem(
                        element = element,
                        relevanceScore = enhancedScore,
                        matchReason = bestMatch.reason,
                        highlightedText = bestMatch.highlightedText,
                    ),
                )
            }
        }

        return results.sortedByDescending { it.relevanceScore }
    }

    override fun getConfiguration(): SearchEngineConfiguration =
        SearchEngineConfiguration(
            name = "Intelligent Search Engine",
            version = "2.0.0",
            parameters =
                mapOf(
                    "strategies" to listOf("exact", "prefix", "contains", "fuzzy", "camelCase", "acronym"),
                    "minRelevanceThreshold" to MIN_RELEVANCE_THRESHOLD,
                    "fuzzyThreshold" to FUZZY_SIMILARITY_THRESHOLD,
                    "bslSpecificBonus" to BSL_SPECIFIC_BONUS,
                ),
        )

    /**
     * Поиск всех возможных совпадений с различными стратегиями
     */
    private fun findAllMatches(
        query: String,
        element: ApiElement,
    ): List<MatchResult> {
        val matches = mutableListOf<MatchResult>()
        val elementName = element.name
        val queryLower = query.lowercase()
        val nameLower = elementName.lowercase()

        // 1. Точное совпадение
        if (queryLower == nameLower) {
            matches.add(
                MatchResult(
                    score = EXACT_MATCH_WEIGHT,
                    reason = MatchReason.ExactMatch("name"),
                    highlightedText = "**$elementName**",
                ),
            )
        }

        // 2. Префиксное совпадение
        if (nameLower.startsWith(queryLower)) {
            val score = PREFIX_MATCH_WEIGHT * (queryLower.length.toDouble() / nameLower.length)
            matches.add(
                MatchResult(
                    score = score,
                    reason = MatchReason.PrefixMatch("name"),
                    highlightedText = "**${elementName.substring(0, query.length)}**${elementName.substring(query.length)}",
                ),
            )
        }

        // 3. Содержание
        if (nameLower.contains(queryLower)) {
            val score = CONTAINS_MATCH_WEIGHT * (queryLower.length.toDouble() / nameLower.length)
            matches.add(
                MatchResult(
                    score = score,
                    reason = MatchReason.ContainsMatch("name"),
                    highlightedText = elementName.replace(query, "**$query**", ignoreCase = true),
                ),
            )
        }

        // 4. CamelCase совпадение
        val camelCaseMatch = findCamelCaseMatch(query, elementName)
        if (camelCaseMatch != null) {
            matches.add(camelCaseMatch)
        }

        // 5. Акроним совпадение
        val acronymMatch = findAcronymMatch(query, elementName)
        if (acronymMatch != null) {
            matches.add(acronymMatch)
        }

        // 6. Fuzzy совпадение
        val fuzzyMatch = findFuzzyMatch(query, elementName)
        if (fuzzyMatch != null) {
            matches.add(fuzzyMatch)
        }

        // 7. Поиск в описании
        val descriptionMatch = findDescriptionMatch(query, element.description)
        if (descriptionMatch != null) {
            matches.add(descriptionMatch)
        }

        return matches
    }

    /**
     * Поиск совпадений в CamelCase нотации
     */
    private fun findCamelCaseMatch(
        query: String,
        elementName: String,
    ): MatchResult? {
        val camelCaseInitials = extractCamelCaseInitials(elementName)
        val queryUpper = query.uppercase()

        return if (camelCaseInitials.contains(queryUpper)) {
            val score = CAMEL_CASE_MATCH_WEIGHT * (queryUpper.length.toDouble() / camelCaseInitials.length)
            MatchResult(
                score = score,
                reason = MatchReason.ContainsMatch("camelCase"),
                highlightedText = highlightCamelCase(elementName, queryUpper),
            )
        } else {
            null
        }
    }

    /**
     * Поиск акронимных совпадений
     */
    private fun findAcronymMatch(
        query: String,
        elementName: String,
    ): MatchResult? {
        val words = elementName.split(Regex("[А-Я]")).filter { it.isNotEmpty() }
        if (words.size < 2) return null

        val acronym = words.map { it.first().uppercase() }.joinToString("")
        val queryUpper = query.uppercase()

        return if (acronym.contains(queryUpper)) {
            val score = ACRONYM_MATCH_WEIGHT * (queryUpper.length.toDouble() / acronym.length)
            MatchResult(
                score = score,
                reason = MatchReason.ContainsMatch("acronym"),
                highlightedText = elementName,
            )
        } else {
            null
        }
    }

    /**
     * Fuzzy поиск с расстоянием Левенштейна
     */
    private fun findFuzzyMatch(
        query: String,
        elementName: String,
    ): MatchResult? {
        val similarity = calculateLevenshteinSimilarity(query.lowercase(), elementName.lowercase())

        return if (similarity >= FUZZY_SIMILARITY_THRESHOLD) {
            val score = FUZZY_MATCH_WEIGHT * similarity
            MatchResult(
                score = score,
                reason = MatchReason.FuzzyMatch("name", similarity),
                highlightedText = null,
            )
        } else {
            null
        }
    }

    /**
     * Поиск в описании элемента
     */
    private fun findDescriptionMatch(
        query: String,
        description: String,
    ): MatchResult? {
        val queryLower = query.lowercase()
        val descLower = description.lowercase()

        return if (descLower.contains(queryLower)) {
            val score = 0.4 * (queryLower.length.toDouble() / descLower.length)
            MatchResult(
                score = score,
                reason = MatchReason.ContainsMatch("description"),
                highlightedText = null,
            )
        } else {
            null
        }
    }

    /**
     * Выбор лучшего совпадения из найденных
     */
    private fun selectBestMatch(matches: List<MatchResult>): MatchResult? {
        if (matches.isEmpty()) return null

        // Если есть точное совпадение, возвращаем его
        val exactMatch = matches.find { it.reason is MatchReason.ExactMatch }
        if (exactMatch != null) return exactMatch

        // Иначе возвращаем совпадение с максимальным скором
        return matches.maxByOrNull { it.score }
    }

    /**
     * Улучшение скора на основе контекста
     */
    private fun enhanceScore(
        baseScore: Double,
        element: ApiElement,
        query: SearchQuery,
    ): Double {
        var enhancedScore = baseScore

        // Бонус за специфичные термины 1С
        if (isBslSpecificTerm(element.name)) {
            enhancedScore += BSL_SPECIFIC_BONUS
        }

        // Бонус за типы (обычно более важные)
        if (element is ru.alkoleft.context.core.domain.api.Type) {
            enhancedScore += TYPE_NAME_BONUS
        }

        // Штраф за очень длинные имена при коротких запросах
        if (query.text.length < 3 && element.name.length > 20) {
            enhancedScore *= 0.8
        }

        return min(1.0, enhancedScore)
    }

    /**
     * Проверка, является ли термин специфичным для 1С
     */
    private fun isBslSpecificTerm(name: String): Boolean {
        val bslTerms =
            setOf(
                "справочник",
                "документ",
                "регистр",
                "отчет",
                "обработка",
                "записать",
                "провести",
                "найти",
                "получить",
                "установить",
                "ссылка",
                "код",
                "наименование",
                "реквизит",
                "табличная",
            )

        return bslTerms.any { term ->
            name.lowercase().contains(term)
        }
    }

    /**
     * Извлечение инициалов из CamelCase
     */
    private fun extractCamelCaseInitials(text: String): String = text.filter { it.isUpperCase() }

    /**
     * Подсветка CamelCase совпадений
     */
    private fun highlightCamelCase(
        text: String,
        query: String,
    ): String {
        return text // Упрощенная реализация
    }

    /**
     * Вычисление сходства Левенштейна
     */
    private fun calculateLevenshteinSimilarity(
        s1: String,
        s2: String,
    ): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val maxLength = max(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)

        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Расстояние Левенштейна
     */
    private fun levenshteinDistance(
        s1: String,
        s2: String,
    ): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] =
                    min(
                        min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost,
                    )
            }
        }

        return dp[len1][len2]
    }

    /**
     * Внутренний класс для результатов совпадений
     */
    private data class MatchResult(
        val score: Double,
        val reason: MatchReason,
        val highlightedText: String?,
    )
}
