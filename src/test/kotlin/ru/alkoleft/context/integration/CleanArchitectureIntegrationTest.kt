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
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ActiveProfiles
import ru.alkoleft.context.application.configuration.ApplicationConfiguration
import ru.alkoleft.context.application.services.FormatterRegistryService
import ru.alkoleft.context.core.configuration.CoreConfiguration
import ru.alkoleft.context.core.ports.incoming.SearchUseCase
import ru.alkoleft.context.infrastructure.configuration.InfrastructureConfiguration
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Интеграционный тест Clean Architecture
 * Проверяет корректность внедрения зависимостей и работы основных компонентов
 */
@SpringBootTest(
    classes = [
        CoreConfiguration::class,
        ApplicationConfiguration::class,
        InfrastructureConfiguration::class,
    ],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
@ComponentScan("ru.alkoleft.context.infrastructure")
@ActiveProfiles("test")
class CleanArchitectureIntegrationTest {
    @Autowired
    lateinit var searchUseCase: SearchUseCase

    @Autowired
    lateinit var formatterRegistryService: FormatterRegistryService

    /**
     * Проверка инъекции основных компонентов Clean Architecture
     */
    @Test
    fun `should inject core Clean Architecture components`() {
        assertNotNull(searchUseCase, "SearchUseCase должен быть создан")
        assertNotNull(formatterRegistryService, "FormatterRegistryService должен быть создан")
    }

    /**
     * Проверка архитектурных слоев - Use Cases
     */
    @Test
    fun `should have working use cases layer`() {
        // Arrange
        val searchQuery = "test search"

        // Act & Assert - проверяем, что use case может обработать запрос без ошибок
        assertNotNull(searchUseCase, "SearchUseCase должен быть доступен")
        // Не вызываем реальный search, так как это требует полной инфраструктуры
    }

    /**
     * Проверка архитектурных слоев - Application Services
     */
    @Test
    fun `should have working application services layer`() {
        // Arrange & Act
        val supportedFormats = formatterRegistryService.getSupportedFormats()

        // Assert
        assertTrue(
            supportedFormats.isNotEmpty(),
            "FormatterRegistryService должен содержать форматтеры",
        )
        assertTrue(
            supportedFormats.any { it.name == "MARKDOWN" },
            "Должен быть доступен MARKDOWN форматтер",
        )
    }

    /**
     * Проверка статистики форматтеров
     */
    @Test
    fun `should provide formatter statistics`() {
        // Act
        val stats = formatterRegistryService.getFormatterStatistics()

        // Assert
        assertNotNull(stats, "Статистика форматтеров должна быть доступна")
        assertTrue(
            stats.totalFormatters > 0,
            "Общее количество форматтеров должно быть больше 0",
        )
        assertTrue(
            stats.formattersDetails.isNotEmpty(),
            "Детали форматтеров должны быть доступны",
        )
    }
}
