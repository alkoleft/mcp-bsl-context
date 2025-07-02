/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.ports.incoming

import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResult

/**
 * Входящий порт для поисковых операций
 * Определяет основные use cases для поиска по API платформы
 */
interface SearchUseCase {
    /**
     * Основной метод поиска по API платформы
     */
    suspend fun search(query: SearchQuery): SearchResult

    /**
     * Получение детальной информации об элементе API по его ID
     */
    suspend fun getDetailedInfo(elementId: String): ApiElement?

    /**
     * Получение информации о члене (методе/свойстве) определенного типа
     */
    suspend fun getMemberInfo(
        typeName: String,
        memberName: String,
    ): ApiElement?

    /**
     * Получение всех членов указанного типа
     */
    suspend fun getTypeMembers(typeName: String): List<ApiElement>

    /**
     * Получение конструкторов для указанного типа
     */
    suspend fun getConstructors(typeName: String): List<ApiElement>
}
