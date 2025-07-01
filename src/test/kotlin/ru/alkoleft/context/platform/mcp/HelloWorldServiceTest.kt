/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.mcp

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertTrue

/**
 * Unit тесты для Hello World MCP сервиса
 *
 * Валидирует:
 * - Kotlin testing framework
 * - MCP Tool functionality
 * - String processing
 * - Kotlin features demonstration
 */
class HelloWorldServiceTest {

    private val service = HelloWorldService()

    @Test
    fun `hello world should return greeting with name`() {
        // Given
        val name = "Kotlin"

        // When
        val result = service.helloWorld(name)

        // Then
        assertTrue(result.contains(name))
        assertTrue(result.contains("Привет"))
        assertTrue(result.contains("успешно"))
    }

    @Test
    fun `hello world should use default name when not provided`() {
        // When
        val result = service.helloWorld()

        // Then
        assertTrue(result.contains("Мир"))
        assertTrue(result.contains("🎉"))
    }

    @Test
    fun `kotlin info should contain system information`() {
        // When
        val result = service.kotlinInfo()

        // Then
        assertAll(
            { assertTrue(result.contains("Kotlin")) },
            { assertTrue(result.contains("Spring Boot")) },
            { assertTrue(result.contains("MCP Server")) },
            { assertTrue(result.contains("Operational")) },
            { assertTrue(result.contains("DTO Migration")) },
            { assertTrue(result.contains("Kotlin DSL")) }
        )
    }

    @Test
    fun `test kotlin features should process items correctly`() {
        // Given
        val items = "kotlin,spring,custom"

        // When
        val result = service.testKotlinFeatures(items)

        // Then
        assertAll(
            { assertTrue(result.contains("Kotlin: Configured")) },
            { assertTrue(result.contains("Spring Boot: Running")) },
            { assertTrue(result.contains("custom: Unknown")) },
            { assertTrue(result.contains("Data Classes")) },
            { assertTrue(result.contains("Extension Functions")) },
            { assertTrue(result.contains("Lambda Expressions")) }
        )
    }

    @Test
    fun `test kotlin features should handle empty input`() {
        // When
        val result = service.testKotlinFeatures("")

        // Then
        assertTrue(result.contains("Kotlin Features Demo"))
        // Should handle gracefully without crashing
    }

    @Test
    fun `test kotlin features should use default items when not provided`() {
        // When
        val result = service.testKotlinFeatures()

        // Then
        assertAll(
            { assertTrue(result.contains("Kotlin: Configured")) },
            { assertTrue(result.contains("Spring Boot: Running")) },
            { assertTrue(result.contains("MCP Server: Active")) }
        )
    }

    @Test
    fun `service should be lightweight and performant`() {
        // Given
        val iterations = 100
        val startTime = System.currentTimeMillis()

        // When
        repeat(iterations) {
            service.helloWorld("Test")
            service.kotlinInfo()
            service.testKotlinFeatures("test")
        }

        // Then
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        // Should complete 300 operations in under 1 second
        assertTrue(duration < 1000, "Service should be performant: ${duration}ms for $iterations iterations")
    }
} 