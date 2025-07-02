/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.ports.outgoing

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.api.DataSource
import ru.alkoleft.context.core.domain.search.ApiElementType

/**
 * Исходящий порт для доступа к данным API платформы
 * Будет реализован в infrastructure слое
 */
interface ApiRepository {
    /**
     * Загрузка всех элементов API из указанного источника
     */
    suspend fun loadElements(source: DataSource): List<ApiElement>

    /**
     * Поиск элемента по точному имени
     */
    suspend fun findByName(
        name: String,
        type: ApiElementType? = null,
    ): ApiElement?

    /**
     * Поиск элементов по префиксу имени
     */
    suspend fun findByNamePrefix(
        prefix: String,
        type: ApiElementType? = null,
    ): List<ApiElement>

    /**
     * Поиск элементов по содержанию в имени
     */
    suspend fun findByNameContains(
        substring: String,
        type: ApiElementType? = null,
    ): List<ApiElement>

    /**
     * Получение всех элементов указанного типа
     */
    suspend fun findByType(type: ApiElementType): List<ApiElement>

    /**
     * Получение членов (методов/свойств) указанного типа
     */
    suspend fun findMembersByTypeName(typeName: String): List<ApiElement>

    /**
     * Получение конструкторов указанного типа
     */
    suspend fun findConstructorsByTypeName(typeName: String): List<ApiElement>

    /**
     * Кэширование элементов для быстрого доступа
     */
    suspend fun cacheElements(elements: List<ApiElement>)

    /**
     * Очистка кэша
     */
    suspend fun clearCache()

    /**
     * Получение статистики репозитория
     */
    suspend fun getStatistics(): RepositoryStatistics
}

/**
 * Статистика репозитория
 */
data class RepositoryStatistics(
    val totalElements: Int,
    val cachedElements: Int,
    val elementsByType: Map<ApiElementType, Int>,
    val elementsBySource: Map<DataSource, Int>,
)
