/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.entities

/**
 * Value Object: Определение параметра
 */
data class ParameterDefinition(
    val name: String,
    val type: String,
    val description: String,
    val required: Boolean = false,
    val defaultValue: String? = null,
)
