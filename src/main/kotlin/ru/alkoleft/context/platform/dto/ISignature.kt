/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

/**
 * Kotlin интерфейс для сигнатур методов и конструкторов
 *
 * Заменяет Java интерфейс ISignature с полной функциональной совместимостью
 */
interface ISignature {
    val name: String
    val description: String
    val params: List<ParameterDefinition>
} 