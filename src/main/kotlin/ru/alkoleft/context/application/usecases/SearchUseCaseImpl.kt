/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.usecases

import org.springframework.stereotype.Service
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchQuery
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.ports.incoming.SearchUseCase
import ru.alkoleft.context.core.services.SearchService

/**
 * Реализация use case для поисковых операций
 * Оркестрирует доменные сервисы для выполнения поисковых запросов
 */
@Service
class SearchUseCaseImpl(
    private val searchService: SearchService,
) : SearchUseCase {
    /**
     * Основной метод поиска по API платформы
     */
    override suspend fun search(query: SearchQuery): SearchResult = searchService.search(query)

    /**
     * Получение детальной информации об элементе API по его ID
     */
    override suspend fun getDetailedInfo(elementId: String): ApiElement? {
        // Извлекаем имя из ID (format: "type_name")
        val elementName = extractElementNameFromId(elementId)
        return searchService.findByExactName(elementName)
    }

    /**
     * Получение информации о члене (методе/свойстве) определенного типа
     */
    override suspend fun getMemberInfo(
        typeName: String,
        memberName: String,
    ): ApiElement? {
        val members = searchService.findMembersByType(typeName)
        return members.find {
            it.name.equals(memberName, ignoreCase = true)
        }
    }

    /**
     * Получение всех членов указанного типа
     */
    override suspend fun getTypeMembers(typeName: String): List<ApiElement> = searchService.findMembersByType(typeName)

    /**
     * Получение конструкторов для указанного типа
     */
    override suspend fun getConstructors(typeName: String): List<ApiElement> = searchService.findConstructorsByType(typeName)

    /**
     * Извлечение имени элемента из его ID
     */
    private fun extractElementNameFromId(elementId: String): String {
        // ID format: "type_name" -> extract "name"
        return elementId.substringAfter("_", elementId)
    }
}
