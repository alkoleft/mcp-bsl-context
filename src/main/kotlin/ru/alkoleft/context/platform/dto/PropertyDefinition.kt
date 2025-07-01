package ru.alkoleft.context.platform.dto

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Kotlin data class для определения свойств типов
 *
 * Заменяет Java record PropertyDefinition с полной JSON совместимостью
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class PropertyDefinition(
    val name: String,
    val nameEn: String,
    val description: String,
    val readonly: Boolean,
    val type: String
) 