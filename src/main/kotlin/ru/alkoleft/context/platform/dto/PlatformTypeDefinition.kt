/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

/**
 * Kotlin data class для определения платформенных типов 1С Предприятие
 *
 * Заменяет Java record PlatformTypeDefinition с полной функциональной совместимостью
 */
data class PlatformTypeDefinition(
    val name: String,
    val description: String,
    val methods: List<MethodDefinition>,
    val properties: List<PropertyDefinition>,
    val constructors: List<ISignature>
) 