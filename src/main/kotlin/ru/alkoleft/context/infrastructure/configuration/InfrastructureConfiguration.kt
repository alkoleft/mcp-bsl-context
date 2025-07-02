/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.alkoleft.context.core.domain.search.SearchAlgorithm
import ru.alkoleft.context.core.ports.outgoing.ApiRepository
import ru.alkoleft.context.core.ports.outgoing.SearchEngine
import ru.alkoleft.context.infrastructure.adapters.outgoing.repositories.PlatformApiRepository
import ru.alkoleft.context.infrastructure.adapters.outgoing.repositories.mappers.DomainModelMapper
import ru.alkoleft.context.infrastructure.adapters.outgoing.search.CompositeSearchEngine
import ru.alkoleft.context.platform.exporter.ExporterLogic
import ru.alkoleft.context.platform.legacy.PlatformContextService

/**
 * Конфигурация инфраструктурного слоя.
 * Настраивает адаптеры, реализации портов и Strategy pattern компоненты
 */
@Configuration
class InfrastructureConfiguration {
    /**
     * Конфигурация Domain Model Mapper
     */
    @Bean
    fun domainModelMapper(): DomainModelMapper = DomainModelMapper()

    /**
     * Конфигурация API Repository
     */
    @Bean
    fun apiRepository(
        platformContextService: PlatformContextService,
        exporterLogic: ExporterLogic,
        domainMapper: DomainModelMapper,
    ): ApiRepository =
        PlatformApiRepository(
            platformContextService,
            exporterLogic,
            domainMapper,
        )

    /**
     * Основной композитный поисковый движок
     * Объединяет все доступные поисковые стратегии
     *
     * Форматтеры и поисковые движки FuzzySearchEngine, IntelligentSearchEngine
     * регистрируются автоматически как @Component (Strategy Pattern)
     */
    @Bean
    fun searchEngine(searchEngines: List<SearchEngine>): SearchEngine {
        // Фильтруем только базовые движки (не комбинированные)
        val baseEngines =
            searchEngines.filter {
                it.algorithm != SearchAlgorithm.HYBRID
            }
        return CompositeSearchEngine(baseEngines)
    }
}
