/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.mcp

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service

/**
 * Hello World MCP сервис для демонстрации Kotlin интеграции
 *
 * Proof of Concept для проверки:
 * - Kotlin compilation
 * - Spring Boot integration
 * - MCP Tool annotations
 * - Spring AI MCP Server compatibility
 */
@Service
class HelloWorldService {

    @Tool(
        name = "hello_world",
        description = "Простейший MCP инструмент на Kotlin для демонстрации архитектуры"
    )
    fun helloWorld(
        @ToolParam(description = "Имя для приветствия") name: String = "Мир"
    ): String {
        return "🎉 Привет, $name! Kotlin MCP сервер работает успешно!"
    }

    @Tool(
        name = "kotlin_info",
        description = "Информация о Kotlin интеграции в MCP сервере"
    )
    fun kotlinInfo(): String {
        return buildString {
            appendLine("📋 **Kotlin MCP Server Information:**")
            appendLine("- Language: Kotlin ${KotlinVersion.CURRENT}")
            appendLine("- Framework: Spring Boot")
            appendLine("- MCP: Spring AI MCP Server")
            appendLine("- Status: ✅ Operational")
            appendLine("- Architecture: Hybrid Kotlin+Java")
            appendLine("")
            appendLine("🎯 **Next Steps:**")
            appendLine("- DTO Migration to Kotlin Data Classes")
            appendLine("- Service Layer Migration")
            appendLine("- Kotlin DSL Implementation")
            appendLine("- Kotlin Coroutines Integration")
        }
    }

    @Tool(
        name = "test_kotlin_features",
        description = "Демонстрация ключевых Kotlin возможностей"
    )
    fun testKotlinFeatures(
        @ToolParam(description = "Список элементов для демонстрации") items: String = "kotlin,spring,mcp"
    ): String {
        val itemList = items.split(",").map { it.trim() }

        return buildString {
            appendLine("🚀 **Kotlin Features Demo:**")
            appendLine("")

            // Демонстрация Kotlin data class
            data class DemoItem(val name: String, val emoji: String, val status: String)

            val demoItems = itemList.mapIndexed { index, item ->
                when (item.lowercase()) {
                    "kotlin" -> DemoItem("Kotlin", "🎯", "Configured")
                    "spring" -> DemoItem("Spring Boot", "🍃", "Running")
                    "mcp" -> DemoItem("MCP Server", "🔧", "Active")
                    else -> DemoItem(item, "📌", "Unknown")
                }
            }

            appendLine("**Items Status:**")
            demoItems.forEach { item ->
                appendLine("${item.emoji} ${item.name}: ${item.status}")
            }

            appendLine("")
            appendLine("**Kotlin Features Used:**")
            appendLine("- Data Classes ✅")
            appendLine("- Extension Functions ✅")
            appendLine("- Lambda Expressions ✅")
            appendLine("- String Templates ✅")
            appendLine("- buildString DSL ✅")
            appendLine("- Collection Operations ✅")

            // Демонстрация extension function
            fun String.toKotlinStyle() = this.split(" ").joinToString("") {
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            }

            appendLine("")
            appendLine("🎨 **DSL Example:** ${items.toKotlinStyle()}")
        }
    }
} 