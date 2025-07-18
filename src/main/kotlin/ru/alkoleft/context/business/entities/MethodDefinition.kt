/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.entities

/**
 * Доменная сущность, представляющая определение метода.
 * Содержит информацию о методе, его параметрах и сигнатуре.
 */
data class MethodDefinition(
    override val name: String,
    override val description: String,
    val returnType: String,
    val signature: List<Signature>,
) : Definition
