/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.mcp

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.cache.annotation.Cacheable
import ru.alkoleft.context.platform.dto.MethodDefinition
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import ru.alkoleft.context.platform.dto.PropertyDefinition
import ru.alkoleft.context.platform.exporter.BaseExporterLogic
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

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
 * Kotlin реализация улучшенного сервиса поиска по API платформы 1С Предприятие через MCP протокол
 * Использует три отдельных индекса для оптимальной производительности
 *
 * Основные улучшения по сравнению с Java версией:
 * - Kotlin coroutines для асинхронной обработки
 * - Типобезопасный API с sealed classes
 * - Функциональное программирование
 * - Immutable data structures
 * - Null safety
 *
 * @deprecated Используйте новую Clean Architecture с McpSearchController
 */
// @Service // Отключен для миграции на Clean Architecture
class PlatformApiSearchService(
    private val contextService: PlatformContextService,
    private val formatter: MarkdownFormatterService,
    private val exporterLogic: BaseExporterLogic,
) {
    companion object {
        private val log = LoggerFactory.getLogger(PlatformApiSearchService::class.java)
    }

    // Три отдельных индекса для оптимальной производительности
    private val globalMethodsIndex = ConcurrentHashMap<String, MethodDefinition>()
    private val globalPropertiesIndex = ConcurrentHashMap<String, PropertyDefinition>()
    private val typesIndex = ConcurrentHashMap<String, PlatformTypeDefinition>()
    private val indexInitialized = AtomicBoolean(false)

    /**
     * Поиск по API платформы 1С Предприятие
     */
    @Tool(
        name = "search",
        description = "Поиск по API платформы 1С Предприятие. Используйте конкретные термины 1С для получения точных результатов.",
    )
    @Cacheable("api-search")
    fun search(
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
        val effectiveLimit = limit ?: 10

        if (query.isBlank()) {
            return "❌ **Ошибка:** Запрос не может быть пустым"
        }

        try {
            ensureIndexInitialized()
        } catch (e: Exception) {
            log.error("Ошибка при инициализации индексов поиска", e)
            return "❌ **Ошибка:** ${e.message}"
        }

        // Нормализация запроса
        val normalizedQuery = query.trim().lowercase()

        // Поиск в соответствующих индексах
        val searchResults = performIntelligentSearch(normalizedQuery, normalizeType(type))

        // Лимитирование результатов
        val limitedResults = searchResults.take(minOf(effectiveLimit, 50))

        // Форматирование результатов
        return formatter.formatSearchResults(query, limitedResults)
    }

    /**
     * Получение детальной информации об API элементе
     */
    @Tool(
        name = "info",
        description = "Получение детальной информации об элементе API платформы 1С. Требует точное имя элемента.",
    )
    @Cacheable("api-info")
    fun getInfo(
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
            ensureIndexInitialized()
        } catch (e: Exception) {
            log.error("Ошибка при инициализации индексов поиска", e)
            return "❌ **Ошибка:** ${e.message}"
        }

        val normalizedName = name.trim().lowercase()
        val normalizedType = normalizeType(type)

        // Поиск точного совпадения в соответствующих индексах
        val result = findExactMatch(normalizedName, normalizedType)

        return result?.let { formatter.formatDetailedInfo(it) }
            ?: "❌ **Не найдено:** $name типа ${type ?: "любого"}"
    }

    /**
     * Получение информации об элементе типа (методе или свойстве)
     */
    @Tool(
        name = "getMember",
        description = "Получение информации о методе или свойстве конкретного типа 1С. Используйте точные имена типов и элементов.",
    )
    @Cacheable("api-member")
    fun getMember(
        @ToolParam(description = "Имя типа 1С. Примеры: 'СправочникСсылка', 'ДокументОбъект', 'Строка', 'Число', 'Дата'")
        typeName: String,
        @ToolParam(description = "Имя метода или свойства типа. Примеры: 'НайтиПоКоду', 'Записать', 'Код', 'Наименование', 'Длина'")
        memberName: String,
    ): String {
        if (typeName.isBlank() || memberName.isBlank()) {
            return "❌ **Ошибка:** Имя типа и имя элемента не могут быть пустыми"
        }

        try {
            ensureIndexInitialized()
        } catch (e: Exception) {
            log.error("Ошибка при инициализации индексов поиска", e)
            return "❌ **Ошибка:** ${e.message}"
        }

        val normalizedTypeName = typeName.trim().lowercase()
        val normalizedMemberName = memberName.trim().lowercase()

        // Поиск типа
        val type =
            typesIndex.values.find {
                it.name.lowercase() == normalizedTypeName
            }

        if (type == null) {
            return "❌ **Тип не найден:** $typeName"
        }

        // Поиск метода в типе
        val method =
            type.methods.find {
                it.name.lowercase() == normalizedMemberName
            }

        if (method != null) {
            return formatter.formatDetailedInfo(method)
        }

        // Поиск свойства в типе
        val property =
            type.properties.find {
                it.name.lowercase() == normalizedMemberName
            }

        if (property != null) {
            return formatter.formatDetailedInfo(property)
        }

        return "❌ **Элемент не найден:** $memberName в типе $typeName"
    }

    /**
     * Получение списка конструкторов для указанного типа 1С
     */
    @Tool(
        name = "getConstructors",
        description = "Получение списка конструкторов для указанного типа 1С. Показывает способы создания объектов данного типа.",
    )
    @Cacheable("api-constructors")
    fun getConstructors(
        @ToolParam(
            description =
                "Имя типа 1С для получения конструкторов. " +
                    "Примеры: 'СправочникМенеджер', 'ДокументМенеджер', 'Запрос', 'ТаблицаЗначений'",
        )
        typeName: String,
    ): String {
        if (typeName.isBlank()) {
            return "❌ **Ошибка:** Имя типа не может быть пустым"
        }

        try {
            ensureIndexInitialized()
        } catch (e: Exception) {
            log.error("Ошибка при инициализации индексов поиска", e)
            return "❌ **Ошибка:** ${e.message}"
        }

        val normalizedTypeName = typeName.trim().lowercase()

        val type =
            typesIndex.values.find {
                it.name.lowercase() == normalizedTypeName
            }

        if (type == null) {
            return "❌ **Тип не найден:** $typeName"
        }

        if (type.constructors.isEmpty()) {
            return "❌ **Конструкторы не найдены** для типа $typeName"
        }

        return formatter.formatConstructors(type.constructors, typeName)
    }

    /**
     * Получение полного списка всех методов и свойств для указанного типа 1С
     */
    @Tool(
        name = "getMembers",
        description = "Получение полного списка всех методов и свойств для указанного типа 1С. Полный справочник API типа.",
    )
    @Cacheable("api-members")
    fun getMembers(
        @ToolParam(
            description =
                "Имя типа 1С для получения полного списка методов и свойств. " +
                    "Примеры: 'СправочникСсылка', 'ДокументОбъект', 'Строка', 'ТаблицаЗначений', 'Запрос'",
        )
        typeName: String,
    ): String {
        if (typeName.isBlank()) {
            return "❌ **Ошибка:** Имя типа не может быть пустым"
        }

        try {
            ensureIndexInitialized()
        } catch (e: Exception) {
            log.error("Ошибка при инициализации индексов поиска", e)
            return "❌ **Ошибка:** ${e.message}"
        }

        val normalizedTypeName = typeName.trim().lowercase()

        val type =
            typesIndex.values.find {
                it.name.lowercase() == normalizedTypeName
            }

        if (type == null) {
            return "❌ **Тип не найден:** $typeName"
        }

        return formatter.formatTypeMembers(type)
    }

    // Приватные методы

    private fun ensureIndexInitialized() {
        if (indexInitialized.get()) return

        synchronized(this) {
            if (!indexInitialized.get()) {
                initializeSearchIndexes()
            }
        }
    }

    private fun initializeSearchIndexes() {
        log.info("Инициализация поисковых индексов...")

        try {
            val contextProvider = contextService.getContextProvider()

            // Инициализация индексов
            globalMethodsIndex.clear()
            globalPropertiesIndex.clear()
            typesIndex.clear()

            // Заполняем индекс глобальных методов и свойств
            val globalContext = contextProvider.globalContext
            if (globalContext != null) {
                exporterLogic.extractMethods(globalContext).forEach { methodDef ->
                    globalMethodsIndex[methodDef.name.lowercase()] = methodDef
                }

                exporterLogic.extractProperties(globalContext).forEach { propertyDef ->
                    globalPropertiesIndex[propertyDef.name.lowercase()] = propertyDef
                }
            }

            // Заполняем индекс типов данных
            val contexts = contextProvider.contexts
            if (contexts != null) {
                exporterLogic.extractTypes(contexts.toList()).forEach { typeDefinition ->
                    typesIndex[typeDefinition.name.lowercase()] = typeDefinition

                    // Добавляем методы и свойства типов в глобальные индексы
                    typeDefinition.methods.forEach { method ->
                        globalMethodsIndex[method.name.lowercase()] = method
                    }

                    typeDefinition.properties.forEach { property ->
                        globalPropertiesIndex[property.name.lowercase()] = property
                    }
                }
            }

            indexInitialized.set(true)
            log.info(
                "Поисковые индексы успешно инициализированы: типы=${typesIndex.size}, методы=${globalMethodsIndex.size}, свойства=${globalPropertiesIndex.size}",
            )
        } catch (e: Exception) {
            log.error("Ошибка при инициализации поисковых индексов", e)
            throw RuntimeException("Не удалось инициализировать поисковые индексы", e)
        }
    }

    private fun normalizeType(type: String?): String? {
        if (type.isNullOrBlank()) return null

        val normalized = type.trim().lowercase()
        return TYPE_ALIASES[normalized] ?: normalized
    }

    private fun performIntelligentSearch(
        query: String,
        type: String?,
    ): List<Any> {
        val words = query.split("\\s+".toRegex()).filter { it.isNotBlank() }

        if (words.isEmpty()) return emptyList()

        // Все возможные типы поиска с приоритетами
        val searchResults = mutableListOf<SearchResult>()

        // 1. Поиск составных типов (compound-type search) - приоритет 1
        if (words.size >= 2) {
            searchResults.addAll(searchCompoundTypes(words.toTypedArray(), query))
        }

        // 2. Поиск "тип.элемент" (type-member search) - приоритет 2
        if (words.size >= 2) {
            searchResults.addAll(searchTypeMember(words.toTypedArray(), query))
        }

        // 3. Обычный поиск (regular search) - приоритет 3
        searchResults.addAll(performRegularSearch(query, type))

        // 4. Поиск по порядку слов (word-order search) - приоритет 4
        if (words.size >= 2) {
            searchResults.addAll(searchWordOrder(words.toTypedArray(), query, type))
        }

        // Удаление дубликатов и сортировка по приоритету
        return removeDuplicates(searchResults)
            .sortedWith(compareBy<SearchResult> { it.priority }.thenByDescending { it.wordsMatched })
            .map { it.item }
    }

    private fun performRegularSearch(
        query: String,
        type: String?,
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val queryLower = query.lowercase()

        when (type) {
            "method" -> {
                globalMethodsIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }
            }

            "property" -> {
                globalPropertiesIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }
            }

            "type" -> {
                typesIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }
            }

            else -> {
                // Поиск по всем типам
                globalMethodsIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }

                globalPropertiesIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }

                typesIndex.values
                    .filter { it.name.lowercase().contains(queryLower) }
                    .forEach { results.add(SearchResult.regular(it, query)) }
            }
        }

        return results
    }

    private fun removeDuplicates(results: List<SearchResult>): List<SearchResult> {
        val seen = mutableSetOf<String>()
        return results.filter { result ->
            val name = getObjectName(result.item)
            seen.add(name)
        }
    }

    private fun searchCompoundTypes(
        words: Array<String>,
        originalQuery: String,
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val variants = generateCompoundVariants(words)

        variants.forEach { variant ->
            typesIndex.values
                .filter { it.name.lowercase().contains(variant.lowercase()) }
                .forEach { type ->
                    val wordsMatched = countWordsInVariant(variant, words)
                    results.add(SearchResult.compoundType(type, wordsMatched, originalQuery))
                }
        }

        return results
    }

    private fun generateCompoundVariants(words: Array<String>): List<String> {
        val variants = mutableListOf<String>()

        // Простое объединение всех слов
        variants.add(words.joinToString(""))

        // Объединение соседних слов
        for (i in 0 until words.size - 1) {
            variants.add(words[i] + words[i + 1])
        }

        // Объединение первого и последнего
        if (words.size > 2) {
            variants.add(words.first() + words.last())
        }

        return variants.distinct()
    }

    private fun countWordsInVariant(
        variant: String,
        originalWords: Array<String>,
    ): Int =
        originalWords.count { word ->
            variant.lowercase().contains(word.lowercase())
        }

    private fun searchTypeMember(
        words: Array<String>,
        originalQuery: String,
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // Пробуем разные комбинации слов как "тип.элемент"
        for (i in words.indices) {
            val typeName = words[i]
            val memberQuery = words.drop(i + 1).joinToString(" ")

            if (memberQuery.isNotEmpty()) {
                typesIndex.values
                    .filter { it.name.lowercase().contains(typeName.lowercase()) }
                    .forEach { type ->
                        val members = searchMembersInType(type, memberQuery)
                        members.forEach { member ->
                            results.add(SearchResult.typeMember(member, originalQuery))
                        }
                    }
            }
        }

        return results
    }

    private fun searchMembersInType(
        type: PlatformTypeDefinition,
        memberQuery: String,
    ): List<Any> {
        val results = mutableListOf<Any>()
        val queryLower = memberQuery.lowercase()

        // Поиск среди методов
        type.methods
            .filter { it.name.lowercase().contains(queryLower) }
            .forEach { results.add(it) }

        // Поиск среди свойств
        type.properties
            .filter { it.name.lowercase().contains(queryLower) }
            .forEach { results.add(it) }

        return results
    }

    private fun searchWordOrder(
        words: Array<String>,
        originalQuery: String,
        type: String?,
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        val searchTargets =
            when (type) {
                "method" -> globalMethodsIndex.values.asSequence()
                "property" -> globalPropertiesIndex.values.asSequence()
                "type" -> typesIndex.values.asSequence()
                else ->
                    sequenceOf(
                        globalMethodsIndex.values,
                        globalPropertiesIndex.values,
                        typesIndex.values,
                    ).flatten()
            }

        searchTargets.forEach { item ->
            val itemName = getObjectName(item)
            val matchingWords = countMatchingWords(itemName, words)

            if (matchingWords > 0) {
                results.add(SearchResult.wordOrder(item, matchingWords, originalQuery))
            }
        }

        return results
    }

    private fun countMatchingWords(
        elementName: String,
        queryWords: Array<String>,
    ): Int {
        val nameLower = elementName.lowercase()
        return queryWords.count { word ->
            nameLower.contains(word.lowercase())
        }
    }

    private fun getObjectName(obj: Any): String =
        when (obj) {
            is MethodDefinition -> obj.name
            is PropertyDefinition -> obj.name
            is PlatformTypeDefinition -> obj.name
            else -> obj.toString()
        }

    private fun findExactMatch(
        name: String,
        type: String?,
    ): Any? =
        when (type) {
            "method" -> globalMethodsIndex[name]
            "property" -> globalPropertiesIndex[name]
            "type" -> typesIndex[name]
            else -> {
                globalMethodsIndex[name]
                    ?: globalPropertiesIndex[name]
                    ?: typesIndex[name]
            }
        }

    /**
     * Результат поиска с метаданными для сортировки
     */
    private data class SearchResult(
        val item: Any,
        val priority: Int, // 1-4, где 1 - высший приоритет
        val wordsMatched: Int, // количество совпавших слов для приоритета
        val matchType: String, // тип совпадения
        val originalQuery: String,
    ) {
        companion object {
            fun compoundType(
                item: Any,
                wordsMatched: Int,
                originalQuery: String,
            ) = SearchResult(item, 1, wordsMatched, "compound-type", originalQuery)

            fun typeMember(
                item: Any,
                originalQuery: String,
            ) = SearchResult(item, 2, 0, "type-member", originalQuery)

            fun regular(
                item: Any,
                originalQuery: String,
            ) = SearchResult(item, 3, 0, "regular", originalQuery)

            fun wordOrder(
                item: Any,
                wordsMatched: Int,
                originalQuery: String,
            ) = SearchResult(item, 4, wordsMatched, "word-order", originalQuery)
        }
    }
}
