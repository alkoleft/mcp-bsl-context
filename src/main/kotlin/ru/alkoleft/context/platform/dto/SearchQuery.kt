/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

/**
 * Data class для поискового запроса в Kotlin DSL
 *
 * Основная структура для типобезопасного поиска по API платформы
 */
data class SearchQuery(
        val text: String,
        val type: ApiType? = null,
        val limit: Int = 10,
        val options: SearchOptions = SearchOptions(),
) {
    /**
     * Ключ для кэширования запроса
     */
    fun cacheKey(): String = "${text}_${type?.name}_${limit}_${options.hashCode()}"
}

/**
 * Data class для опций поиска
 *
 * Конфигурирует поведение поискового алгоритма
 */
data class SearchOptions(
        var intelligent: Boolean = true,
        var includeInherited: Boolean = false,
        var caseSensitive: Boolean = false,
        var exactMatch: Boolean = false,
)

/**
 * Data class для результатов поиска
 *
 * Содержит результаты поиска по всем типам API элементов
 */
data class SearchResults(
        val methods: List<MethodDefinition> = emptyList(),
        val properties: List<PropertyDefinition> = emptyList(),
        val types: List<PlatformTypeDefinition> = emptyList(),
)
