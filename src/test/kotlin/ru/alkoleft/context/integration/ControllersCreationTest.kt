/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.integration

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.alkoleft.context.infrastructure.adapters.incoming.mcp.McpContextController
import ru.alkoleft.context.infrastructure.adapters.incoming.mcp.McpSearchController
import kotlin.test.assertNotNull

/**
 * Тест создания MCP контроллеров
 */
@SpringBootTest(
    classes = [TestApplicationWithoutMcp::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ActiveProfiles("test")
class ControllersCreationTest {
    @Autowired(required = false)
    private var mcpSearchController: McpSearchController? = null

    @Autowired(required = false)
    private var mcpContextController: McpContextController? = null

    @Test
    fun `should create MCP controllers`() {
        assertNotNull(mcpSearchController, "McpSearchController should be created")
        assertNotNull(mcpContextController, "McpContextController should be created")
    }
}
