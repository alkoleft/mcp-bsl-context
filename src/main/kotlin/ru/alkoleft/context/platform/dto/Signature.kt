/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

/**
 * Kotlin data class для сигнатур методов
 *
 * Заменяет Java record Signature с реализацией интерфейса ISignature
 */
data class Signature(
        override val name: String,
        override val description: String,
        override val params: List<ParameterDefinition>,
) : ISignature
