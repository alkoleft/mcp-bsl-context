/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import ru.alkoleft.context.platform.mcp.PlatformApiSearchService

/**
 * Spring Boot приложение для MCP сервера платформы 1С Предприятие
 *
 * Обновленная Kotlin версия с MCP-only архитектурой.
 * Удален CLI интерфейс, остался только MCP сервер.
 */
@SpringBootApplication
@EnableCaching
class McpServerApplication {
    @Bean
    fun platformTools(platformApiSearchService: PlatformApiSearchService): ToolCallbackProvider =
        MethodToolCallbackProvider
            .builder()
            .toolObjects(platformApiSearchService)
            .build()
}

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}
