/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.alkoleft.context.business.persistent.PlatformContextRepository
import ru.alkoleft.context.business.services.ResponseFormatterService
import ru.alkoleft.context.infrastructure.persistent.repositories.PlatformRepository
import ru.alkoleft.context.infrastructure.persistent.storage.PlatformContextLoader
import ru.alkoleft.context.infrastructure.persistent.storage.PlatformContextStorage
import ru.alkoleft.context.infrastructure.search.SearchEngine
import ru.alkoleft.context.infrastructure.search.SimpleSearchEngine
import ru.alkoleft.context.presentation.formatters.MarkdownFormatterService
import java.nio.file.Path

@Configuration
class PlatformContextConfiguration {
    @Bean
    fun platformContextRepository(
        searchEngine: SearchEngine,
        storage: PlatformContextStorage,
    ): PlatformContextRepository = PlatformRepository(searchEngine, storage)

    @Bean
    fun searchEngine(storage: PlatformContextStorage): SearchEngine = SimpleSearchEngine(storage)

    @Bean
    fun contextStorage(
        loader: PlatformContextLoader,
        @Value("\${platform.context.path}") platformPath: Path,
    ): PlatformContextStorage = PlatformContextStorage(loader, platformPath)

    @Bean
    fun contextLoader(): PlatformContextLoader = PlatformContextLoader()

    @Bean
    fun formatter(): ResponseFormatterService = MarkdownFormatterService()
}
