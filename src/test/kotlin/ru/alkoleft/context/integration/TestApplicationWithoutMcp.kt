/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.integration

import org.springframework.ai.mcp.server.autoconfigure.McpServerAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

/**
 * Тестовое приложение без MCP tools для диагностики
 * Используется для проверки, что основная архитектура работает корректно
 */
@SpringBootApplication(
    exclude = [McpServerAutoConfiguration::class],
)
@EnableCaching
@ComponentScan(
    basePackages = ["ru.alkoleft.context"],
    useDefaultFilters = false,
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [
                org.springframework.stereotype.Component::class,
                org.springframework.stereotype.Service::class,
                org.springframework.stereotype.Repository::class,
            ],
        ),
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [ru.alkoleft.context.platform.mcp.PlatformContextLoader::class],
        ),
    ],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ["ru\\.alkoleft\\.context\\.platform\\..*"],
        ),
    ],
)
class TestApplicationWithoutMcp
