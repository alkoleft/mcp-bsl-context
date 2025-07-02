/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.services

import ru.alkoleft.context.core.domain.api.DataSource
import ru.alkoleft.context.core.ports.incoming.ContextStatus
import ru.alkoleft.context.core.ports.incoming.PlatformContext
import ru.alkoleft.context.core.ports.outgoing.ApiRepository
import java.time.Instant

/**
 * Доменный сервис для управления контекстом платформы
 * Координирует загрузку и обновление данных из различных источников
 */
class ContextService(
    private val apiRepository: ApiRepository,
) {
    private var currentContext: PlatformContext? = null
    private val loadingErrors = mutableListOf<String>()

    /**
     * Загрузка контекста платформы из указанного пути
     */
    suspend fun loadContext(path: String): PlatformContext {
        loadingErrors.clear()

        try {
            // Загрузка элементов из основного источника
            val elements = apiRepository.loadElements(DataSource.BSL_CONTEXT)

            // Кэширование элементов для быстрого доступа
            apiRepository.cacheElements(elements)

            val context =
                PlatformContext(
                    path = path,
                    loadedAt = Instant.now(),
                    totalElements = elements.size,
                    sources = setOf(DataSource.BSL_CONTEXT),
                    version = extractVersionFromPath(path),
                )

            currentContext = context
            return context
        } catch (e: Exception) {
            val error = "Failed to load context from path: $path. Error: ${e.message}"
            loadingErrors.add(error)
            throw IllegalStateException(error, e)
        }
    }

    /**
     * Обновление текущего контекста
     */
    suspend fun refreshContext(): PlatformContext {
        val context =
            currentContext
                ?: throw IllegalStateException("No context loaded. Call loadContext() first.")

        return loadContext(context.path)
    }

    /**
     * Получение статуса текущего контекста
     */
    suspend fun getContextStatus(): ContextStatus {
        val context = currentContext

        return if (context != null) {
            val stats = apiRepository.getStatistics()
            ContextStatus(
                isLoaded = true,
                lastUpdate = context.loadedAt,
                elementsCount = context.totalElements,
                errors = loadingErrors.toList(),
            )
        } else {
            ContextStatus(
                isLoaded = false,
                lastUpdate = null,
                elementsCount = 0,
                errors = loadingErrors.toList(),
            )
        }
    }

    /**
     * Получение списка доступных источников данных
     */
    suspend fun getAvailableDataSources(): List<DataSource> {
        // В текущей версии поддерживается только BSL_CONTEXT
        // В будущем здесь будет логика определения доступных источников
        return listOf(DataSource.BSL_CONTEXT)
    }

    /**
     * Загрузка данных из дополнительного источника
     */
    suspend fun loadAdditionalSource(source: DataSource): PlatformContext {
        val context =
            currentContext
                ?: throw IllegalStateException("No base context loaded. Call loadContext() first.")

        try {
            val elements = apiRepository.loadElements(source)
            apiRepository.cacheElements(elements)

            val stats = apiRepository.getStatistics()
            val updatedContext =
                context.copy(
                    totalElements = stats.totalElements,
                    sources = context.sources + source,
                    loadedAt = Instant.now(),
                )

            currentContext = updatedContext
            return updatedContext
        } catch (e: Exception) {
            val error = "Failed to load additional source: $source. Error: ${e.message}"
            loadingErrors.add(error)
            throw IllegalStateException(error, e)
        }
    }

    /**
     * Очистка всех кэшированных данных
     */
    suspend fun clearContext() {
        apiRepository.clearCache()
        currentContext = null
        loadingErrors.clear()
    }

    /**
     * Получение текущего контекста
     */
    fun getCurrentContext(): PlatformContext? = currentContext

    /**
     * Проверка, загружен ли контекст
     */
    fun isContextLoaded(): Boolean = currentContext != null

    /**
     * Извлечение версии из пути (простая эвристика)
     */
    private fun extractVersionFromPath(path: String): String? {
        // Простая логика для извлечения версии из пути
        // Может быть улучшена в будущем
        val versionRegex = """(\d+\.\d+(?:\.\d+)?)""".toRegex()
        return versionRegex.find(path)?.value
    }
}
