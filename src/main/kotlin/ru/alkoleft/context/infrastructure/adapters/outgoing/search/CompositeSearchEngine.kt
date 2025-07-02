/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.search

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.MatchReason
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResultItem
import ru.alkoleft.context.core.ports.outgoing.SearchEngine
import ru.alkoleft.context.core.ports.outgoing.SearchEngineConfiguration

/**
 * Композитный поисковый движок, объединяющий результаты нескольких алгоритмов
 * Реализует гибридный подход для максимальной точности поиска
 *
 * Создается через @Bean метод в InfrastructureConfiguration
 */
class CompositeSearchEngine(
    private val searchEngines: List<SearchEngine>,
) : SearchEngine {
    override val algorithm = SearchAlgorithm.HYBRID

    companion object {
        // Веса для различных алгоритмов в композитном поиске
        private val ALGORITHM_WEIGHTS: Map<SearchAlgorithm, Double> =
            mapOf(
                SearchAlgorithm.INTELLIGENT to 0.4,
                SearchAlgorithm.FUZZY to 0.3,
                SearchAlgorithm.FULL_TEXT to 0.2,
                SearchAlgorithm.SEMANTIC to 0.1,
            )

        private const val MIN_CONSENSUS_SCORE = 0.2
        private const val CONSENSUS_BOOST = 0.1
    }

    override suspend fun search(
        query: SearchQuery,
        candidates: List<ApiElement>,
    ): List<SearchResultItem> {
        // Получаем результаты от всех доступных движков
        val engineResults = mutableMapOf<SearchAlgorithm, List<SearchResultItem>>()

        searchEngines.forEach { engine ->
            if (engine.algorithm != SearchAlgorithm.HYBRID) { // Избегаем рекурсии
                try {
                    val results = engine.search(query, candidates)
                    engineResults[engine.algorithm] = results
                } catch (e: Exception) {
                    // Логируем ошибку, но продолжаем с другими движками
                    // log.warn("Search engine ${engine.algorithm} failed", e)
                }
            }
        }

        // Объединяем результаты и вычисляем композитные скоры
        val compositeResults = combineResults(engineResults, query)

        return compositeResults.sortedByDescending { it.relevanceScore }
    }

    override fun getConfiguration(): SearchEngineConfiguration =
        SearchEngineConfiguration(
            name = "Composite Search Engine",
            version = "1.0.0",
            parameters =
                mapOf<String, Any>(
                    "childEngines" to searchEngines.map { it.algorithm.name },
                    "algorithmWeights" to ALGORITHM_WEIGHTS,
                    "minConsensusScore" to MIN_CONSENSUS_SCORE,
                    "consensusBoost" to CONSENSUS_BOOST,
                ),
        )

    /**
     * Объединение результатов от различных поисковых движков
     */
    private fun combineResults(
        engineResults: Map<SearchAlgorithm, List<SearchResultItem>>,
        query: SearchQuery,
    ): List<SearchResultItem> {
        // Собираем все уникальные элементы
        val allElements = mutableSetOf<ApiElement>()
        engineResults.values.forEach { results ->
            results.forEach { item ->
                allElements.add(item.element)
            }
        }

        // Для каждого элемента вычисляем композитный скор
        val compositeResults = mutableListOf<SearchResultItem>()

        allElements.forEach { element ->
            val compositeItem = createCompositeItem(element, engineResults, query)
            if (compositeItem.relevanceScore >= MIN_CONSENSUS_SCORE) {
                compositeResults.add(compositeItem)
            }
        }

        return compositeResults
    }

    /**
     * Создание композитного элемента результата
     */
    private fun createCompositeItem(
        element: ApiElement,
        engineResults: Map<SearchAlgorithm, List<SearchResultItem>>,
        query: SearchQuery,
    ): SearchResultItem {
        val scores = mutableListOf<Double>()
        val reasons = mutableListOf<MatchReason>()
        var bestHighlightedText: String? = null
        var consensusCount = 0

        // Собираем скоры от всех движков
        engineResults.forEach { (algorithm, results) ->
            val item = results.find { it.element.id == element.id }
            if (item != null) {
                val weight = ALGORITHM_WEIGHTS[algorithm] ?: 0.1
                scores.add(item.relevanceScore * weight)
                reasons.add(item.matchReason)

                // Выбираем лучший highlighted text
                if (bestHighlightedText == null && item.highlightedText != null) {
                    bestHighlightedText = item.highlightedText
                }

                consensusCount++
            }
        }

        // Вычисляем итоговый скор
        val baseScore = scores.sum()
        val consensusBoost = if (consensusCount > 1) CONSENSUS_BOOST * (consensusCount - 1) else 0.0
        val finalScore = kotlin.math.min(1.0, baseScore + consensusBoost)

        // Определяем композитную причину совпадения
        val compositeReason = createCompositeMatchReason(reasons)

        return SearchResultItem(
            element = element,
            relevanceScore = finalScore,
            matchReason = compositeReason,
            highlightedText = bestHighlightedText,
        )
    }

    /**
     * Создание композитной причины совпадения
     */
    private fun createCompositeMatchReason(reasons: List<MatchReason>): MatchReason =
        if (reasons.size == 1) {
            reasons.first()
        } else {
            MatchReason.MultipleMatches(reasons)
        }

    /**
     * Анализ консенсуса между алгоритмами
     */
    private fun analyzeConsensus(engineResults: Map<SearchAlgorithm, List<SearchResultItem>>): ConsensusAnalysis {
        val elementCounts = mutableMapOf<String, Int>()
        val totalEngines = engineResults.size

        engineResults.values.forEach { results ->
            results.forEach { item ->
                elementCounts[item.element.id] = (elementCounts[item.element.id] ?: 0) + 1
            }
        }

        val highConsensusElements = elementCounts.filter { it.value >= totalEngines / 2 }
        val consensusRate = highConsensusElements.size.toDouble() / elementCounts.size

        return ConsensusAnalysis(
            totalElements = elementCounts.size,
            highConsensusElements = highConsensusElements.size,
            consensusRate = consensusRate,
            participatingEngines = totalEngines,
        )
    }

    /**
     * Адаптивная настройка весов на основе производительности
     */
    private fun adaptWeights(
        engineResults: Map<SearchAlgorithm, List<SearchResultItem>>,
        query: SearchQuery,
    ): Map<SearchAlgorithm, Double> {
        // Базовые веса
        val adaptedWeights: MutableMap<SearchAlgorithm, Double> = ALGORITHM_WEIGHTS.toMutableMap()

        // Анализируем качество результатов каждого движка
        engineResults.forEach { (algorithm, results) ->
            val quality = assessResultQuality(results, query)
            val currentWeight = adaptedWeights[algorithm] ?: 0.1

            // Адаптируем вес на основе качества
            val adaptationFactor =
                if (quality > 0.7) {
                    1.1
                } else if (quality < 0.3) {
                    0.9
                } else {
                    1.0
                }
            adaptedWeights[algorithm] = currentWeight * adaptationFactor
        }

        // Нормализуем веса
        val totalWeight = adaptedWeights.values.sum()
        adaptedWeights.forEach { (algorithm, weight) ->
            adaptedWeights[algorithm] = weight / totalWeight
        }

        return adaptedWeights
    }

    /**
     * Оценка качества результатов движка
     */
    private fun assessResultQuality(
        results: List<SearchResultItem>,
        query: SearchQuery,
    ): Double {
        if (results.isEmpty()) return 0.0

        // Простая метрика качества на основе средней релевантности и распределения скоров
        val averageScore = results.map { it.relevanceScore }.average()
        val scoreVariance = calculateVariance(results.map { it.relevanceScore })

        // Хорошие результаты имеют высокий средний скор и разумное распределение
        return averageScore * (1.0 - scoreVariance)
    }

    /**
     * Вычисление дисперсии
     */
    private fun calculateVariance(scores: List<Double>): Double {
        if (scores.isEmpty()) return 0.0

        val mean = scores.average()
        val variance = scores.map { (it - mean) * (it - mean) }.average()

        return kotlin.math.sqrt(variance) / (scores.maxOrNull() ?: 1.0)
    }

    /**
     * Результаты анализа консенсуса
     */
    data class ConsensusAnalysis(
        val totalElements: Int,
        val highConsensusElements: Int,
        val consensusRate: Double,
        val participatingEngines: Int,
    )
}
