/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.services

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.ApiElementType
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.domain.search.SearchStatistics
import ru.alkoleft.context.core.ports.outgoing.ApiRepository
import ru.alkoleft.context.core.ports.outgoing.SearchEngine

/**
 * Доменный сервис для поисковых операций
 * Координирует работу между репозиторием, поисковыми движками и ранжированием
 */
class SearchService(
    private val apiRepository: ApiRepository,
    private val searchEngines: List<SearchEngine>,
    private val rankingService: RankingService,
) {
    /**
     * Основной метод поиска с использованием указанного алгоритма
     */
    suspend fun search(query: SearchQuery): SearchResult {
        val startTime = System.currentTimeMillis()

        // Валидация запроса
        validateQuery(query)

        // Получение кандидатов для поиска
        val candidates = getCandidates(query)

        // Выбор подходящего поискового движка
        val searchEngine =
            selectSearchEngine(query.options.algorithm)
                ?: throw IllegalArgumentException("Search algorithm ${query.options.algorithm} is not supported")

        // Выполнение поиска
        val searchItems = searchEngine.search(query, candidates)

        // Ранжирование результатов
        val rankedItems = rankingService.rankResults(searchItems, query)

        // Применение лимита
        val limitedItems = rankedItems.take(query.options.limit)

        val executionTime = System.currentTimeMillis() - startTime

        return SearchResult(
            query = query,
            items = limitedItems,
            totalCount = rankedItems.size,
            executionTimeMs = executionTime,
            algorithm = searchEngine.algorithm,
        )
    }

    /**
     * Поиск элемента по точному имени
     */
    suspend fun findByExactName(
        name: String,
        elementType: ApiElementType? = null,
    ): ApiElement? = apiRepository.findByName(name, elementType)

    /**
     * Получение всех членов указанного типа
     */
    suspend fun findMembersByType(typeName: String): List<ApiElement> = apiRepository.findMembersByTypeName(typeName)

    /**
     * Получение конструкторов указанного типа
     */
    suspend fun findConstructorsByType(typeName: String): List<ApiElement> = apiRepository.findConstructorsByTypeName(typeName)

    /**
     * Получение статистики поиска
     */
    suspend fun getSearchStatistics(): SearchStatistics {
        val repoStats = apiRepository.getStatistics()
        return SearchStatistics(
            totalElements = repoStats.totalElements,
            searchedElements = repoStats.cachedElements,
            matchedElements = 0, // Будет обновлено после поиска
            algorithmUsed = SearchAlgorithm.INTELLIGENT, // По умолчанию
            cacheHit = false,
        )
    }

    /**
     * Валидация поискового запроса
     */
    private fun validateQuery(query: SearchQuery) {
        require(query.text.isNotBlank()) { "Search text cannot be blank" }
        require(query.options.limit > 0) { "Search limit must be positive" }
        require(query.options.limit <= 100) { "Search limit cannot exceed 100" }
    }

    /**
     * Получение кандидатов для поиска на основе типов и источников
     */
    private suspend fun getCandidates(query: SearchQuery): List<ApiElement> {
        val allCandidates = mutableListOf<ApiElement>()

        for (source in query.options.dataSources) {
            val elements = apiRepository.loadElements(source)
            allCandidates.addAll(elements)
        }

        // Фильтрация по типам элементов
        return if (query.options.elementTypes.size == ApiElementType.values().size) {
            allCandidates
        } else {
            allCandidates.filter { element ->
                val elementType =
                    when (element) {
                        is ru.alkoleft.context.core.domain.api.Method -> ApiElementType.METHOD
                        is ru.alkoleft.context.core.domain.api.Property -> ApiElementType.PROPERTY
                        is ru.alkoleft.context.core.domain.api.Type -> ApiElementType.TYPE
                        is ru.alkoleft.context.core.domain.api.Constructor -> ApiElementType.CONSTRUCTOR
                    }
                elementType in query.options.elementTypes
            }
        }
    }

    /**
     * Выбор подходящего поискового движка
     */
    private fun selectSearchEngine(algorithm: SearchAlgorithm): SearchEngine? = searchEngines.find { it.supports(algorithm) }
}
