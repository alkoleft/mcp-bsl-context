/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.incoming.mcp

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import ru.alkoleft.context.application.dto.SearchRequest
import ru.alkoleft.context.application.services.SearchApplicationService
import ru.alkoleft.context.core.ports.outgoing.FormatType

// Алиасы типов для удобства LLM - расширено русскоязычными алиасами
private val TYPE_ALIASES =
    mapOf(
        // Существующие английские алиасы
        "object" to "type",
        "class" to "type",
        "datatype" to "type",
        // Русскоязычные алиасы для type
        "объект" to "type",
        "класс" to "type",
        "тип" to "type",
        "структура" to "type",
        "данные" to "type",
        // Русскоязычные алиасы для method
        "метод" to "method",
        "функция" to "method",
        "процедура" to "method",
        // Русскоязычные алиасы для property
        "свойство" to "property",
        "реквизит" to "property",
        "поле" to "property",
        "атрибут" to "property",
    )

/**
 * Тонкий MCP адаптер для поисковых операций
 * Заменяет толстый PlatformApiSearchService, делегируя бизнес-логику в application слой
 */
@Service
class McpSearchController(
    private val searchApplicationService: SearchApplicationService,
) {
    companion object {
        private val log = LoggerFactory.getLogger(McpSearchController::class.java)
    }

    /**
     * Поиск по API платформы 1С Предприятие
     */
    @Tool(
        name = "search",
        description = "Поиск по API платформы 1С Предприятие. Используйте конкретные термины 1С для получения точных результатов.",
    )
    @Cacheable("mcp-search")
    suspend fun search(
        @ToolParam(
            description =
                "Поисковый запрос. Используйте конкретные термины из 1С: " +
                    "методы ('НайтиПоСсылке', 'ВыполнитьОбработку'), " +
                    "типы ('Справочник', 'Документ'), " +
                    "свойства ('Ссылка', 'Код', 'Наименование')",
        )
        query: String,
        @ToolParam(
            description = "Тип искомого элемента API: 'method' - методы, 'property' - свойства, 'type' - типы данных, null - все типы",
        )
        type: String? = null,
        @ToolParam(description = "Максимальное количество результатов (по умолчанию 10, максимум 50)")
        limit: Int? = null,
    ): String {
        if (query.isBlank()) {
            return "❌ **Ошибка:** Запрос не может быть пустым"
        }

        try {
            val searchRequest = createSearchRequest(query, type, limit)
            val response = searchApplicationService.performSearch(searchRequest)

            log.info(
                "Search completed: query='{}', type='{}', results={}, time={}ms",
                query,
                type,
                response.totalCount,
                response.executionTimeMs,
            )

            return response.result
        } catch (e: Exception) {
            log.error("Search error for query '$query'", e)
            return "❌ **Ошибка поиска:** ${e.message}"
        }
    }

    /**
     * Получение детальной информации об API элементе
     */
    @Tool(
        name = "info",
        description = "Получение детальной информации об элементе API платформы 1С. Требует точное имя элемента.",
    )
    @Cacheable("mcp-info")
    suspend fun getInfo(
        @ToolParam(description = "Точное имя элемента API в 1С. Примеры: 'НайтиПоСсылке', 'СправочникСсылка', 'Ссылка', 'Код'")
        name: String,
        @ToolParam(
            description =
                "Уточнение типа элемента: " +
                    "'method' - метод/функция, " +
                    "'property' - свойство/реквизит, " +
                    "'type' - тип данных, null - автоматическое определение",
        )
        type: String? = null,
    ): String {
        if (name.isBlank()) {
            return "❌ **Ошибка:** Имя элемента не может быть пустым"
        }

        try {
            // Используем поиск с точным совпадением для получения детальной информации
            val searchRequest =
                SearchRequest(
                    query = name,
                    exactMatch = true,
                    elementTypes =
                        type?.let { listOf(normalizeType(it).uppercase()) }
                            ?: listOf("METHOD", "PROPERTY", "TYPE", "CONSTRUCTOR"),
                    limit = 1,
                )

            val response = searchApplicationService.performSearch(searchRequest)

            return if (response.hasResults) {
                response.result
            } else {
                "❌ **Не найдено:** $name типа ${type ?: "любого"}"
            }
        } catch (e: Exception) {
            log.error("Info retrieval error for '$name'", e)
            return "❌ **Ошибка:** ${e.message}"
        }
    }

    /**
     * Получение информации об элементе типа (методе или свойстве)
     */
    @Tool(
        name = "getMember",
        description = "Получение информации о методе или свойстве конкретного типа 1С. Используйте точные имена типов и элементов.",
    )
    @Cacheable("mcp-member")
    suspend fun getMember(
        @ToolParam(description = "Имя типа 1С. Примеры: 'СправочникСсылка', 'ДокументОбъект', 'Строка', 'Число', 'Дата'")
        typeName: String,
        @ToolParam(description = "Имя метода или свойства типа. Примеры: 'НайтиПоКоду', 'Записать', 'Код', 'Наименование', 'Длина'")
        memberName: String,
    ): String {
        if (typeName.isBlank() || memberName.isBlank()) {
            return "❌ **Ошибка:** Имя типа и имя элемента не могут быть пустыми"
        }

        try {
            val result =
                searchApplicationService.getMemberInfo(
                    typeName = typeName,
                    memberName = memberName,
                )

            return result ?: "❌ **Не найдено:** элемент '$memberName' в типе '$typeName'"
        } catch (e: Exception) {
            log.error("Member info error for '$typeName.$memberName'", e)
            return "❌ **Ошибка:** ${e.message}"
        }
    }

    /**
     * Получение всех элементов указанного типа
     */
    @Tool(
        name = "getMembers",
        description = "Получение всех методов и свойств указанного типа 1С. Полезно для изучения API типа.",
    )
    @Cacheable("mcp-members")
    suspend fun getMembers(
        @ToolParam(description = "Имя типа 1С. Примеры: 'СправочникСсылка', 'ДокументОбъект', 'Строка', 'Число'")
        typeName: String,
    ): String {
        if (typeName.isBlank()) {
            return "❌ **Ошибка:** Имя типа не может быть пустым"
        }

        try {
            val result = searchApplicationService.getTypeMembers(typeName)
            return result
        } catch (e: Exception) {
            log.error("Members retrieval error for '$typeName'", e)
            return "❌ **Ошибка:** ${e.message}"
        }
    }

    /**
     * Получение конструкторов указанного типа
     */
    @Tool(
        name = "getConstructors",
        description = "Получение всех конструкторов указанного типа 1С. Показывает способы создания объектов типа.",
    )
    @Cacheable("mcp-constructors")
    suspend fun getConstructors(
        @ToolParam(description = "Имя типа 1С. Примеры: 'Массив', 'Структура', 'СписокЗначений'")
        typeName: String,
    ): String {
        if (typeName.isBlank()) {
            return "❌ **Ошибка:** Имя типа не может быть пустым"
        }

        try {
            val result = searchApplicationService.getConstructors(typeName)
            return result
        } catch (e: Exception) {
            log.error("Constructors retrieval error for '$typeName'", e)
            return "❌ **Ошибка:** ${e.message}"
        }
    }

    /**
     * Создание SearchRequest из параметров MCP tool
     */
    private fun createSearchRequest(
        query: String,
        type: String?,
        limit: Int?,
    ): SearchRequest {
        val normalizedType = type?.let { normalizeType(it) }
        val elementTypes =
            when (normalizedType) {
                "method" -> listOf("METHOD")
                "property" -> listOf("PROPERTY")
                "type" -> listOf("TYPE")
                "constructor" -> listOf("CONSTRUCTOR")
                else -> listOf("METHOD", "PROPERTY", "TYPE", "CONSTRUCTOR")
            }

        return SearchRequest(
            query = query.trim(),
            elementTypes = elementTypes,
            limit = minOf(limit ?: 10, 50),
            formatType = FormatType.MARKDOWN,
        )
    }

    /**
     * Нормализация типа с учетом алиасов
     */
    private fun normalizeType(type: String): String {
        val normalized = type.lowercase().trim()
        return TYPE_ALIASES[normalized] ?: normalized
    }
}
