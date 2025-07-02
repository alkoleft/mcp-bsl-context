/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.services

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.alkoleft.context.application.dto.SearchRequest
import ru.alkoleft.context.application.dto.SearchResponse
import ru.alkoleft.context.core.domain.search.ApiElementType
import ru.alkoleft.context.core.domain.search.Language
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.domain.search.SearchContext
import ru.alkoleft.context.core.domain.search.SearchOptions
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.ports.incoming.SearchUseCase
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.FormattingContext

/**
 * Application Service для координации поисковых операций
 * Высокоуровневый сервис, который оркестрирует use cases и форматирование
 */
@Service
class SearchApplicationService(
    private val searchUseCase: SearchUseCase,
    private val formatterRegistry: FormatterRegistryService,
) {
    /**
     * Выполнение поиска с форматированием результата
     */
    @Cacheable("formatted-search")
    suspend fun performSearch(request: SearchRequest): SearchResponse {
        // Преобразование DTO в доменную модель
        val searchQuery = mapToSearchQuery(request)

        // Выполнение поиска
        val searchResult = searchUseCase.search(searchQuery)

        // Форматирование результата
        val formattedResult =
            formatterRegistry.formatSearchResults(
                result = searchResult,
                formatType = request.formatType,
                context =
                    FormattingContext(
                        includeMetadata = request.includeMetadata,
                        includeStatistics = request.includeStatistics,
                        maxLength = request.maxLength,
                        language = request.language,
                        highlightMatches = request.highlightMatches,
                    ),
            )

        return SearchResponse(
            query = request.query,
            result = formattedResult,
            totalCount = searchResult.totalCount,
            executionTimeMs = searchResult.executionTimeMs,
            algorithm = searchResult.algorithm.name,
            hasMore = searchResult.limitReached,
        )
    }

    /**
     * Получение детальной информации об элементе с форматированием
     */
    @Cacheable("formatted-info")
    suspend fun getElementInfo(
        elementId: String,
        formatType: FormatType = FormatType.MARKDOWN,
    ): String? {
        val element = searchUseCase.getDetailedInfo(elementId)
        return element?.let {
            formatterRegistry.formatDetailedInfo(it, formatType)
        }
    }

    /**
     * Получение информации о члене типа с форматированием
     */
    @Cacheable("formatted-member")
    suspend fun getMemberInfo(
        typeName: String,
        memberName: String,
        formatType: FormatType = FormatType.MARKDOWN,
    ): String? {
        val member = searchUseCase.getMemberInfo(typeName, memberName)
        return member?.let {
            formatterRegistry.formatDetailedInfo(it, formatType)
        }
    }

    /**
     * Получение всех членов типа с форматированием
     */
    @Cacheable("formatted-members")
    suspend fun getTypeMembers(
        typeName: String,
        formatType: FormatType = FormatType.MARKDOWN,
    ): String {
        val members = searchUseCase.getTypeMembers(typeName)
        return formatterRegistry.formatElementList(
            elements = members,
            title = "Члены типа $typeName",
            formatType = formatType,
        )
    }

    /**
     * Получение конструкторов типа с форматированием
     */
    @Cacheable("formatted-constructors")
    suspend fun getConstructors(
        typeName: String,
        formatType: FormatType = FormatType.MARKDOWN,
    ): String {
        val constructors = searchUseCase.getConstructors(typeName)
        return formatterRegistry.formatElementList(
            elements = constructors,
            title = "Конструкторы типа $typeName",
            formatType = formatType,
        )
    }

    /**
     * Преобразование DTO запроса в доменную модель
     */
    private fun mapToSearchQuery(request: SearchRequest): SearchQuery {
        val options =
            SearchOptions(
                algorithm = SearchAlgorithm.valueOf(request.algorithm),
                elementTypes = request.elementTypes.map { ApiElementType.valueOf(it) }.toSet(),
                limit = request.limit,
                includeInherited = request.includeInherited,
                caseSensitive = request.caseSensitive,
                exactMatch = request.exactMatch,
            )

        val context =
            SearchContext(
                parentType = request.parentType,
                preferredLanguage = Language.valueOf(request.language),
                includeDeprecated = request.includeDeprecated,
                relevanceThreshold = request.relevanceThreshold,
            )

        return SearchQuery(
            text = request.query,
            options = options,
            context = context,
        )
    }
}
