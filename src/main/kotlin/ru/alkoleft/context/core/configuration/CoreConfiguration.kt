/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.alkoleft.context.core.ports.outgoing.ApiRepository
import ru.alkoleft.context.core.ports.outgoing.SearchEngine
import ru.alkoleft.context.core.services.ContextService
import ru.alkoleft.context.core.services.RankingService
import ru.alkoleft.context.core.services.SearchService

/**
 * Конфигурация доменного слоя (Core)
 * Настраивает доменные сервисы и их взаимодействие
 */
@Configuration
class CoreConfiguration {
    /**
     * Сервис ранжирования результатов поиска
     */
    @Bean
    fun rankingService(): RankingService = RankingService()

    /**
     * Основной поисковый сервис домена
     */
    @Bean
    fun searchService(
        apiRepository: ApiRepository,
        searchEngines: List<SearchEngine>,
        rankingService: RankingService,
    ): SearchService =
        SearchService(
            apiRepository = apiRepository,
            searchEngines = searchEngines,
            rankingService = rankingService,
        )

    /**
     * Сервис работы с контекстом
     */
    @Bean
    fun contextService(apiRepository: ApiRepository): ContextService = ContextService(apiRepository)
}
