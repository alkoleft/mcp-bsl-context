/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.repositories

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.api.DataSource
import ru.alkoleft.context.core.domain.search.ApiElementType
import ru.alkoleft.context.core.ports.outgoing.ApiRepository
import ru.alkoleft.context.core.ports.outgoing.RepositoryStatistics
import ru.alkoleft.context.infrastructure.adapters.outgoing.repositories.mappers.DomainModelMapper
import ru.alkoleft.context.platform.exporter.ExporterLogic
import ru.alkoleft.context.platform.mcp.PlatformContextLoader
import ru.alkoleft.context.platform.mcp.PlatformContextService
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository адаптер для интеграции с существующим BSL Context
 * Преобразует старые DTO в новые доменные модели
 */
@Repository
class BslApiRepository(
    private val contextLoader: PlatformContextLoader,
    private val contextService: PlatformContextService,
    private val exporterLogic: ExporterLogic,
    private val domainMapper: DomainModelMapper,
) : ApiRepository {
    companion object {
        private val log = LoggerFactory.getLogger(BslApiRepository::class.java)
    }

    // Кэш для быстрого доступа к элементам
    private val elementsCache = ConcurrentHashMap<String, ApiElement>()
    private val elementsByType = ConcurrentHashMap<ApiElementType, List<ApiElement>>()
    private var isInitialized = false

    /**
     * Загрузка всех элементов API из указанного источника
     */
    override suspend fun loadElements(source: DataSource): List<ApiElement> =
        when (source) {
            DataSource.BSL_CONTEXT -> loadFromBslContext()
            else -> {
                log.warn("Data source $source is not supported yet")
                emptyList()
            }
        }

    /**
     * Поиск элемента по точному имени
     */
    override suspend fun findByName(
        name: String,
        type: ApiElementType?,
    ): ApiElement? {
        ensureInitialized()

        val normalizedName = name.lowercase()

        return if (type != null) {
            elementsByType[type]?.find { it.name.lowercase() == normalizedName }
        } else {
            elementsCache.values.find { it.name.lowercase() == normalizedName }
        }
    }

    /**
     * Поиск элементов по префиксу имени
     */
    override suspend fun findByNamePrefix(
        prefix: String,
        type: ApiElementType?,
    ): List<ApiElement> {
        ensureInitialized()

        val normalizedPrefix = prefix.lowercase()

        return if (type != null) {
            elementsByType[type]?.filter { it.name.lowercase().startsWith(normalizedPrefix) } ?: emptyList()
        } else {
            elementsCache.values.filter { it.name.lowercase().startsWith(normalizedPrefix) }
        }
    }

    /**
     * Поиск элементов по содержанию в имени
     */
    override suspend fun findByNameContains(
        substring: String,
        type: ApiElementType?,
    ): List<ApiElement> {
        ensureInitialized()

        val normalizedSubstring = substring.lowercase()

        return if (type != null) {
            elementsByType[type]?.filter { it.name.lowercase().contains(normalizedSubstring) } ?: emptyList()
        } else {
            elementsCache.values.filter { it.name.lowercase().contains(normalizedSubstring) }
        }
    }

    /**
     * Получение всех элементов указанного типа
     */
    override suspend fun findByType(type: ApiElementType): List<ApiElement> {
        ensureInitialized()
        return elementsByType[type] ?: emptyList()
    }

    /**
     * Получение членов (методов/свойств) указанного типа
     */
    override suspend fun findMembersByTypeName(typeName: String): List<ApiElement> {
        ensureInitialized()

        val normalizedTypeName = typeName.lowercase()

        return elementsCache.values.filter { element ->
            when (element) {
                is ru.alkoleft.context.core.domain.api.Method ->
                    element.parentType?.name?.lowercase() == normalizedTypeName

                is ru.alkoleft.context.core.domain.api.Property ->
                    element.parentType?.name?.lowercase() == normalizedTypeName

                else -> false
            }
        }
    }

    /**
     * Получение конструкторов указанного типа
     */
    override suspend fun findConstructorsByTypeName(typeName: String): List<ApiElement> {
        ensureInitialized()

        val normalizedTypeName = typeName.lowercase()

        return elementsByType[ApiElementType.CONSTRUCTOR]?.filter { element ->
            when (element) {
                is ru.alkoleft.context.core.domain.api.Constructor ->
                    element.parentType.name.lowercase() == normalizedTypeName

                else -> false
            }
        } ?: emptyList()
    }

    /**
     * Кэширование элементов для быстрого доступа
     */
    override suspend fun cacheElements(elements: List<ApiElement>) {
        elementsCache.clear()
        elementsByType.clear()

        elements.forEach { element ->
            elementsCache[element.id] = element

            val type =
                when (element) {
                    is ru.alkoleft.context.core.domain.api.Method -> ApiElementType.METHOD
                    is ru.alkoleft.context.core.domain.api.Property -> ApiElementType.PROPERTY
                    is ru.alkoleft.context.core.domain.api.Type -> ApiElementType.TYPE
                    is ru.alkoleft.context.core.domain.api.Constructor -> ApiElementType.CONSTRUCTOR
                }

            elementsByType.compute(type) { _, existing ->
                (existing ?: emptyList()) + element
            }
        }

        isInitialized = true
        log.info("Cached {} elements", elements.size)
    }

    /**
     * Очистка кэша
     */
    override suspend fun clearCache() {
        elementsCache.clear()
        elementsByType.clear()
        isInitialized = false
        log.info("Repository cache cleared")
    }

    /**
     * Получение статистики репозитория
     */
    override suspend fun getStatistics(): RepositoryStatistics =
        RepositoryStatistics(
            totalElements = elementsCache.size,
            cachedElements = elementsCache.size,
            elementsByType = elementsByType.mapValues { it.value.size },
            elementsBySource = mapOf(DataSource.BSL_CONTEXT to elementsCache.size),
        )

    /**
     * Загрузка данных из BSL Context
     */
    private suspend fun loadFromBslContext(): List<ApiElement> {
        log.info("Loading elements from BSL Context")

        try {
            // Получаем ContextProvider из PlatformContextService
            val contextProvider = contextService.getContextProvider()
            val elements = mutableListOf<ApiElement>()

            // Загружаем глобальные методы и свойства
            val globalContext = contextProvider.globalContext
            if (globalContext != null) {
                // Глобальные методы
                exporterLogic.extractMethods(globalContext).forEach { methodDto ->
                    val domainMethod = domainMapper.mapMethodDefinitionToMethod(methodDto, isGlobal = true)
                    elements.add(domainMethod)
                }

                // Глобальные свойства
                exporterLogic.extractProperties(globalContext).forEach { propertyDto ->
                    val domainProperty = domainMapper.mapPropertyDefinitionToProperty(propertyDto)
                    elements.add(domainProperty)
                }
            }

            // Загружаем типы и их члены
            val contexts = contextProvider.contexts
            if (contexts != null) {
                exporterLogic.extractTypes(contexts.toList()).forEach { typeDto ->
                    val domainType = domainMapper.mapPlatformTypeToType(typeDto)
                    elements.add(domainType)

                    // Добавляем методы и свойства типа
                    elements.addAll(domainType.methods)
                    elements.addAll(domainType.properties)
                    elements.addAll(domainType.constructors)
                }
            }

            log.info("Loaded {} elements from BSL Context", elements.size)
            return elements
        } catch (e: Exception) {
            log.error("Failed to load elements from BSL Context", e)
            throw RuntimeException("Failed to load BSL Context elements: ${e.message}", e)
        }
    }

    /**
     * Обеспечение инициализации кэша
     */
    private suspend fun ensureInitialized() {
        if (!isInitialized) {
            val elements = loadElements(DataSource.BSL_CONTEXT)
            cacheElements(elements)
        }
    }
}
