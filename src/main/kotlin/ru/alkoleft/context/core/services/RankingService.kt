/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.services

import ru.alkoleft.context.core.domain.search.ApiElementType
import ru.alkoleft.context.core.domain.search.MatchReason
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResultItem
import kotlin.math.max

/**
 * Доменный сервис для ранжирования результатов поиска
 * Применяет различные стратегии ранжирования для повышения релевантности
 */
class RankingService {
    companion object {
        // Веса для различных типов совпадений
        private const val EXACT_MATCH_WEIGHT = 1.0
        private const val PREFIX_MATCH_WEIGHT = 0.9
        private const val CONTAINS_MATCH_WEIGHT = 0.7
        private const val FUZZY_MATCH_WEIGHT = 0.6
        private const val SEMANTIC_MATCH_WEIGHT = 0.8

        // Бонусы для типов элементов
        private const val TYPE_BONUS = 0.1
        private const val METHOD_BONUS = 0.05
        private const val PROPERTY_BONUS = 0.03
        private const val CONSTRUCTOR_BONUS = 0.02

        // Минимальный порог релевантности
        private const val MIN_RELEVANCE_THRESHOLD = 0.1
    }

    /**
     * Ранжирование результатов поиска по релевантности
     */
    fun rankResults(
        items: List<SearchResultItem>,
        query: SearchQuery,
    ): List<SearchResultItem> {
        // Применение дополнительного скоринга
        val enhancedItems =
            items.map { item ->
                val enhancedScore = calculateEnhancedScore(item, query)
                item.copy(relevanceScore = enhancedScore)
            }

        // Фильтрация по порогу релевантности
        val filteredItems =
            enhancedItems.filter {
                it.relevanceScore >= query.context.relevanceThreshold
            }

        // Сортировка по убыванию релевантности
        return filteredItems.sortedByDescending { it.relevanceScore }
    }

    /**
     * Вычисление улучшенного скора релевантности
     */
    private fun calculateEnhancedScore(
        item: SearchResultItem,
        query: SearchQuery,
    ): Double {
        var score = item.relevanceScore

        // Бонус за тип совпадения
        score += getMatchReasonBonus(item.matchReason)

        // Бонус за тип элемента
        score += getElementTypeBonus(item.element)

        // Бонус за точность совпадения с контекстом
        score += getContextBonus(item, query)

        // Нормализация скора в диапазоне [0, 1]
        return max(0.0, minOf(1.0, score))
    }

    /**
     * Бонус за тип совпадения
     */
    private fun getMatchReasonBonus(reason: MatchReason): Double =
        when (reason) {
            is MatchReason.ExactMatch -> EXACT_MATCH_WEIGHT * 0.2
            is MatchReason.PrefixMatch -> PREFIX_MATCH_WEIGHT * 0.15
            is MatchReason.ContainsMatch -> CONTAINS_MATCH_WEIGHT * 0.1
            is MatchReason.FuzzyMatch -> FUZZY_MATCH_WEIGHT * 0.1 * reason.similarity
            is MatchReason.SemanticMatch -> SEMANTIC_MATCH_WEIGHT * 0.15 * reason.similarity
            is MatchReason.MultipleMatches -> {
                // Для множественных совпадений берем максимальный бонус
                reason.reasons.maxOfOrNull { getMatchReasonBonus(it) } ?: 0.0
            }
        }

    /**
     * Бонус за тип элемента API
     */
    private fun getElementTypeBonus(element: ru.alkoleft.context.core.domain.api.ApiElement): Double =
        when (element) {
            is ru.alkoleft.context.core.domain.api.Type -> TYPE_BONUS
            is ru.alkoleft.context.core.domain.api.Method -> METHOD_BONUS
            is ru.alkoleft.context.core.domain.api.Property -> PROPERTY_BONUS
            is ru.alkoleft.context.core.domain.api.Constructor -> CONSTRUCTOR_BONUS
        }

    /**
     * Бонус за соответствие контексту поиска
     */
    private fun getContextBonus(
        item: SearchResultItem,
        query: SearchQuery,
    ): Double {
        var bonus = 0.0

        // Бонус за соответствие родительскому типу
        query.context.parentType?.let { parentType ->
            when (val element = item.element) {
                is ru.alkoleft.context.core.domain.api.Method -> {
                    if (element.parentType?.name?.contains(parentType, ignoreCase = true) == true) {
                        bonus += 0.1
                    }
                }

                is ru.alkoleft.context.core.domain.api.Property -> {
                    if (element.parentType?.name?.contains(parentType, ignoreCase = true) == true) {
                        bonus += 0.1
                    }
                }

                is ru.alkoleft.context.core.domain.api.Constructor -> {
                    if (element.parentType.name.contains(parentType, ignoreCase = true)) {
                        bonus += 0.1
                    }
                }

                else -> {} // Для типов бонус не применяется
            }
        }

        return bonus
    }

    /**
     * Фильтрация результатов по порогу релевантности
     */
    fun filterByRelevance(
        items: List<SearchResultItem>,
        threshold: Double = MIN_RELEVANCE_THRESHOLD,
    ): List<SearchResultItem> = items.filter { it.relevanceScore >= threshold }

    /**
     * Группировка результатов по типу элемента
     */
    fun groupByElementType(items: List<SearchResultItem>): Map<ApiElementType, List<SearchResultItem>> =
        items.groupBy { item ->
            when (item.element) {
                is ru.alkoleft.context.core.domain.api.Method -> ApiElementType.METHOD
                is ru.alkoleft.context.core.domain.api.Property -> ApiElementType.PROPERTY
                is ru.alkoleft.context.core.domain.api.Type -> ApiElementType.TYPE
                is ru.alkoleft.context.core.domain.api.Constructor -> ApiElementType.CONSTRUCTOR
            }
        }
}
