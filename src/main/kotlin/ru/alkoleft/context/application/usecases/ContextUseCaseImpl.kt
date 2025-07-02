/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.usecases

import org.springframework.stereotype.Service
import ru.alkoleft.context.core.domain.api.DataSource
import ru.alkoleft.context.core.ports.incoming.ContextStatus
import ru.alkoleft.context.core.ports.incoming.ContextUseCase
import ru.alkoleft.context.core.ports.incoming.PlatformContext
import ru.alkoleft.context.core.services.ContextService

/**
 * Реализация use case для управления контекстом платформы
 * Координирует загрузку и обновление данных платформы
 */
@Service
class ContextUseCaseImpl(
    private val contextService: ContextService,
) : ContextUseCase {
    /**
     * Загрузка контекста платформы из указанного пути
     */
    override suspend fun loadContext(path: String): PlatformContext = contextService.loadContext(path)

    /**
     * Обновление текущего контекста платформы
     */
    override suspend fun refreshContext(): PlatformContext = contextService.refreshContext()

    /**
     * Получение статуса текущего контекста
     */
    override suspend fun getContextStatus(): ContextStatus = contextService.getContextStatus()

    /**
     * Получение списка доступных источников данных
     */
    override suspend fun getAvailableDataSources(): List<DataSource> = contextService.getAvailableDataSources()
}
