/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

/**
 * Enum для типов API элементов платформы 1С Предприятие
 *
 * Используется в Kotlin DSL для типобезопасного поиска
 */
enum class ApiType {
    METHOD,
    PROPERTY,
    TYPE,
    CONSTRUCTOR,
}
