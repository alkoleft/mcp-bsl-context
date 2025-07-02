/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.ports.outgoing

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResultItem

/**
 * Исходящий порт для поисковых движков
 * Реализует Strategy pattern для различных алгоритмов поиска
 */
interface SearchEngine {
    /**
     * Тип алгоритма поиска, который реализует этот движок
     */
    val algorithm: SearchAlgorithm

    /**
     * Поиск элементов по запросу
     */
    suspend fun search(
        query: SearchQuery,
        candidates: List<ApiElement>,
    ): List<SearchResultItem>

    /**
     * Проверка, поддерживает ли движок указанный алгоритм
     */
    fun supports(algorithm: SearchAlgorithm): Boolean = this.algorithm == algorithm

    /**
     * Получение конфигурации движка
     */
    fun getConfiguration(): SearchEngineConfiguration
}

/**
 * Конфигурация поискового движка
 */
data class SearchEngineConfiguration(
    val name: String,
    val version: String,
    val parameters: Map<String, Any> = emptyMap(),
    val performanceMetrics: PerformanceMetrics? = null,
)

/**
 * Метрики производительности движка
 */
data class PerformanceMetrics(
    val averageSearchTimeMs: Double,
    val searchesPerformed: Long,
    val accuracy: Double? = null,
)
