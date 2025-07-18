/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.valueobjects

import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition

/**
 * Value Object: Поисковый запрос
 *
 * Immutable объект представляющий поисковый запрос с опциями
 */
data class SearchQuery(
    val query: String,
    val apiType: ApiType? = null,
    val includeMethods: Boolean = true,
    val includeProperties: Boolean = true,
    val maxResults: Int = 10,
    val searchOptions: SearchOptions = SearchOptions(),
) {
    /**
     * Проверяет, является ли запрос пустым
     */
    fun isEmpty(): Boolean = query.trim().isEmpty()

    /**
     * Проверяет, является ли запрос валидным
     */
    fun isValid(): Boolean = !isEmpty() && maxResults > 0 && maxResults <= 50

    /**
     * Создает нормализованную версию запроса
     */
    fun normalize(): SearchQuery = copy(query = query.trim().lowercase())

    /**
     * Ключ для кэширования запроса
     */
    fun cacheKey(): String = "${query}_${apiType?.name}_${maxResults}_${searchOptions.hashCode()}"

    /**
     * Получение нормализованного текста для поиска
     */
    fun getNormalizedText(): String = query.lowercase()

    /**
     * Проверяет, нужно ли искать методы
     */
    fun shouldSearchMethods(): Boolean = includeMethods && (apiType == null || apiType == ApiType.METHOD)

    /**
     * Проверяет, нужно ли искать свойства
     */
    fun shouldSearchProperties(): Boolean = includeProperties && (apiType == null || apiType == ApiType.PROPERTY)

    /**
     * Проверяет, нужно ли искать типы
     */
    fun shouldSearchTypes(): Boolean = apiType == null || apiType == ApiType.TYPE

    /**
     * Создает запрос для поиска по точному имени
     */
    fun toExactMatchQuery(): SearchQuery =
        copy(
            searchOptions = searchOptions.copy(exactMatch = true, intelligent = false),
        )

    /**
     * Создает запрос для поиска по частичному имени
     */
    fun toPartialMatchQuery(): SearchQuery =
        copy(
            searchOptions = searchOptions.copy(exactMatch = false, intelligent = true),
        )
}

/**
 * Value Object: Опции поиска
 *
 * Immutable объект конфигурирующий поведение поискового алгоритма
 */
data class SearchOptions(
    val intelligent: Boolean = true,
    val includeInherited: Boolean = false,
    val caseSensitive: Boolean = false,
    val exactMatch: Boolean = false,
) {
    /**
     * Проверка необходимости интеллектуального поиска
     */
    fun shouldUseIntelligentSearch(): Boolean = intelligent && !exactMatch

    /**
     * Проверка необходимости точного совпадения
     */
    fun shouldUseExactMatch(): Boolean = exactMatch

    /**
     * Проверка необходимости учета регистра
     */
    fun shouldBeCaseSensitive(): Boolean = caseSensitive

    /**
     * Проверка необходимости включения наследуемых элементов
     */
    fun shouldIncludeInherited(): Boolean = includeInherited
}

/**
 * Value Object: Результаты поиска
 *
 * Immutable объект содержащий результаты поиска по всем типам API элементов
 */
data class SearchResults(
    val methods: List<MethodDefinition> = emptyList(),
    val properties: List<PropertyDefinition> = emptyList(),
    val types: List<PlatformTypeDefinition> = emptyList(),
) {
    /**
     * Общее количество результатов
     */
    fun getTotalCount(): Int = methods.size + properties.size + types.size

    /**
     * Проверка наличия результатов
     */
    fun hasResults(): Boolean = getTotalCount() > 0

    /**
     * Получение результатов по типу
     */
    fun getResultsByType(type: ApiType): List<Any> =
        when (type) {
            ApiType.METHOD -> methods
            ApiType.PROPERTY -> properties
            ApiType.TYPE -> types
            ApiType.CONSTRUCTOR -> emptyList() // Конструкторы включены в методы
        }

    /**
     * Ограничивает результаты по лимиту
     */
    fun limit(maxResults: Int): SearchResults =
        copy(
            methods = methods.take(maxResults),
            properties = properties.take(maxResults),
            types = types.take(maxResults),
        )

    /**
     * Объединяет результаты с другими
     */
    fun merge(other: SearchResults): SearchResults =
        copy(
            methods = methods + other.methods,
            properties = properties + other.properties,
            types = types + other.types,
        )
}
