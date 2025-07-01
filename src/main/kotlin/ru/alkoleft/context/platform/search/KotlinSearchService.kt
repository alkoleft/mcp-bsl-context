/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.search

import kotlinx.coroutines.*
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.alkoleft.context.platform.dto.ApiType
import ru.alkoleft.context.platform.dto.SearchQuery
import ru.alkoleft.context.platform.dto.SearchResults
import ru.alkoleft.context.platform.mcp.PlatformApiSearchService

/**
 * Kotlin Search Service с DSL поддержкой
 *
 * Предоставляет типобезопасный API для поиска по платформе 1С Предприятие
 * Интегрируется с существующим Java сервисом для обратной совместимости
 */
@Service
class KotlinSearchService(
    private val javaSearchService: PlatformApiSearchService
) {

    /**
     * Типобезопасный поиск с использованием Kotlin DSL
     */
    suspend fun searchTyped(block: SearchContext.() -> SearchQuery): SearchResults = withContext(Dispatchers.IO) {
        val query = search(block)
        executeSearch(query)
    }

    /**
     * MCP Tool метод с поддержкой Kotlin DSL синтаксиса
     * Обеспечивает обратную совместимость со строковыми параметрами
     */
    @Tool(
        name = "kotlin_search",
        description = "Типобезопасный поиск по API платформы 1С Предприятие с использованием Kotlin DSL"
    )
    @Cacheable("kotlin-search")
    suspend fun search(
        @ToolParam(description = "Поисковый запрос") query: String,
        @ToolParam(description = "Тип API элемента: method, property, type") type: String? = null,
        @ToolParam(description = "Лимит результатов") limit: Int? = null,
        @ToolParam(description = "Режим интеллектуального поиска") intelligent: Boolean? = null
    ): String = withContext(Dispatchers.IO) {

        // Создание поискового запроса через DSL
        val searchQuery = search {
            query(query) {
                type?.let { t -> ApiType.fromString(t)?.let { type(it) } }
                limit?.let { limit(it) }
                intelligent?.let {
                    options {
                        this.intelligent = it
                    }
                }
            }
        }

        // Выполнение поиска
        executeSearch(searchQuery)

        // Делегирование форматирования к Java сервису для совместимости
        javaSearchService.search(query, type, limit ?: 10)
    }

    /**
     * Демонстрационный Tool для показа возможностей DSL
     */
    @Tool(
        name = "kotlin_search_demo",
        description = "Демонстрация возможностей Kotlin DSL для поиска"
    )
    fun searchDemo(): String {
        return buildString {
            appendLine("🎯 **Kotlin Search DSL Demo:**")
            appendLine("")
            appendLine("**Синтаксис:**")
            appendLine("```kotlin")
            appendLine("search {")
            appendLine("    query(\"справочник\") {")
            appendLine("        type(ApiType.METHOD)")
            appendLine("        limit(10)")
            appendLine("        options {")
            appendLine("            intelligent = true")
            appendLine("            exactMatch = false")
            appendLine("        }")
            appendLine("    }")
            appendLine("}")
            appendLine("```")
            appendLine("")
            appendLine("**Преимущества:**")
            appendLine("- ✅ Типобезопасность на этапе компиляции")
            appendLine("- ✅ IDE поддержка с автодополнением")
            appendLine("- ✅ Readable DSL синтаксис")
            appendLine("- ✅ Kotlin Coroutines для асинхронности")
            appendLine("- ✅ Интеграция с Spring Cache")
            appendLine("- ✅ Обратная совместимость с Java API")
        }
    }

    /**
     * Внутренний метод для выполнения поиска
     */
    private suspend fun executeSearch(query: SearchQuery): SearchResults = withContext(Dispatchers.IO) {
        // Пока делегируем к Java сервису, в будущем заменим на чистую Kotlin реализацию
        javaSearchService.search(
            query.text,
            query.type?.toStringType(),
            query.limit
        )

        // Заглушка для возврата структурированных результатов
        // В полной реализации здесь будет парсинг результатов и создание SearchResults
        SearchResults()
    }

    /**
     * Асинхронный поиск с корутинами (для будущего развития)
     */
    suspend fun searchAsync(queries: List<SearchQuery>): List<SearchResults> = coroutineScope {
        queries.map { query ->
            async { executeSearch(query) }
        }.awaitAll()
    }
} 