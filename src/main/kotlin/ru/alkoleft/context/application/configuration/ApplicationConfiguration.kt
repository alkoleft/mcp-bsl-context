/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.alkoleft.context.application.services.FormatterRegistryService
import ru.alkoleft.context.application.services.SearchApplicationService
import ru.alkoleft.context.application.usecases.SearchUseCaseImpl
import ru.alkoleft.context.core.ports.incoming.SearchUseCase
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter
import ru.alkoleft.context.core.services.SearchService

/**
 * Конфигурация слоя приложения
 * Настраивает Use Cases и Application Services
 */
@Configuration
class ApplicationConfiguration {
    /**
     * Use Case для поисковых операций
     */
    @Bean
    fun searchUseCase(searchService: SearchService): SearchUseCase = SearchUseCaseImpl(searchService)

    /**
     * Application Service для поисковых операций
     * Оркестрирует взаимодействие между контроллерами и доменом
     */
    @Bean
    fun searchApplicationService(
        searchUseCase: SearchUseCase,
        formatterRegistryService: FormatterRegistryService,
    ): SearchApplicationService =
        SearchApplicationService(
            searchUseCase = searchUseCase,
            formatterRegistry = formatterRegistryService,
        )

    /**
     * Registry для управления форматтерами
     * Spring автоматически инжектит все бины типа ResultFormatter
     */
    @Bean
    fun formatterRegistryService(formatters: List<ResultFormatter>): FormatterRegistryService = FormatterRegistryService(formatters)
}
