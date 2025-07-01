/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Kotlin data class для определения параметров методов
 *
 * Заменяет Java record ParameterDefinition с полной JSON совместимостью
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParameterDefinition(
        val required: Boolean,
        val name: String,
        val description: String,
        val type: String,
)
