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
    val type: String
) 