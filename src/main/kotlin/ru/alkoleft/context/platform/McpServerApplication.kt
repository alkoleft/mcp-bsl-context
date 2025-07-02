/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import ru.alkoleft.context.infrastructure.adapters.incoming.mcp.McpContextController
import ru.alkoleft.context.infrastructure.adapters.incoming.mcp.McpSearchController

/**
 * Spring Boot приложение для MCP сервера платформы 1С Предприятие
 *
 * Обновленная версия с Clean Architecture.
 * Использует Hexagonal Architecture + DDD + Strategy Pattern.
 */
@SpringBootApplication
@EnableCaching
@ComponentScan("ru.alkoleft.context")
class McpServerApplication {
    /**
     * Регистрация MCP tools для новой Clean Architecture
     * Используем тонкие контроллеры вместо толстого сервиса
     */
    @Bean
    fun mcpTools(
        mcpSearchController: McpSearchController,
        mcpContextController: McpContextController,
    ): ToolCallbackProvider =
        MethodToolCallbackProvider
            .builder()
            .toolObjects(mcpSearchController, mcpContextController)
            .build()
}

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}
