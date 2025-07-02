/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.integration

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

/**
 * Минимальный Spring тест для диагностики конфигурации
 * Проверяет загрузку только основных конфигураций без MCP компонентов
 */
@SpringBootTest(
    classes = [ru.alkoleft.context.platform.McpServerApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ActiveProfiles("test")
class MinimalSpringTest {
    @Test
    fun `context should load successfully`() {
        // Просто проверяем, что контекст загружается
        // Если тест проходит, значит основные конфигурации работают
    }
}
