/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

/**
 * Главный класс приложения MCP Server
 *
 * Архитектура приложения (Clean Architecture + SOLID + DDD):
 *
 * ┌─────────────────────────────────────────────────────────────┐
 * │                    PRESENTATION LAYER                       │
 * │  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
 * │  │   MCP Server    │  │   REST API      │  │   STDIO      │ │
 * │  │   Components    │  │   Controllers   │  │   Handlers   │ │
 * │  └─────────────────┘  └─────────────────┘  └──────────────┘ │
 * └─────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                   APPLICATION LAYER                         │
 * │  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
 * │  │  Use Cases      │  │  Application    │  │  DTOs        │ │
 * │  │  Services       │  │  Services       │  │  Mappers     │ │
 * │  └─────────────────┘  └─────────────────┘  └──────────────┘ │
 * └─────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                     DOMAIN LAYER                            │
 * │  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
 * │  │   Entities      │  │  Value Objects  │  │  Aggregates  │ │
 * │  │   Repositories  │  │  Domain         │  │  Services    │ │
 * │  │   Interfaces    │  │  Events         │  │  Exceptions  │ │
 * │  └─────────────────┘  └─────────────────┘  └──────────────┘ │
 * └─────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 * ┌─────────────────────────────────────────────────────────────┐
 * │                  INFRASTRUCTURE LAYER                       │
 * │  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐ │
 * │  │  Repository     │  │  External       │  │  Persistence │ │
 * │  │  Implementations│  │  Services       │  │  & Caching   │ │
 * │  │  Formatters     │  │  Integrations   │  │  & Export    │ │
 * │  └─────────────────┘  └─────────────────┘  └──────────────┘ │
 * └─────────────────────────────────────────────────────────────┘
 *
 * Принципы:
 * - SOLID: Single Responsibility, Open/Closed, Liskov Substitution,
 *          Interface Segregation, Dependency Inversion
 * - DDD: Domain-Driven Design с Entities, Value Objects, Aggregates
 * - Clean Architecture: Dependency Rule, слоистая архитектура
 */
@SpringBootApplication
@ComponentScan(
    basePackages = [
        "ru.alkoleft.context.presentation",
        "ru.alkoleft.context.application",
        "ru.alkoleft.context.domain",
        "ru.alkoleft.context.infrastructure",
    ],
)
class McpServerApplication

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}
