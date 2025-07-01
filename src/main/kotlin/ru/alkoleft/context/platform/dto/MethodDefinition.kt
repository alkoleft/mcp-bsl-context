/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Kotlin data class для определения методов типов
 *
 * Заменяет Java record MethodDefinition с полной функциональной совместимостью
 */
data class MethodDefinition(
    val name: String,
    val description: String,
    val signature: List<Signature>,
    @JsonProperty("return") val returnType: String,
) {
    /**
     * Получение определения типа возврата
     *
     * Эквивалент Java метода getReturnTypeDefinition()
     */
    fun getReturnTypeDefinition(): TypeDefinition = TypeDefinition(returnType, "Возвращаемое значение")

    /**
     * Вспомогательный data class для типа возврата
     *
     * Заменяет вложенный Java record TypeDefinition
     */
    data class TypeDefinition(
        val name: String,
        val description: String,
    )
}
