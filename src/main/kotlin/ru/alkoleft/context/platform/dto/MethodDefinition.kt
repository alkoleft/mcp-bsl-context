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
    @JsonProperty("return") val returnType: String
) {
    /**
     * Получение определения типа возврата
     *
     * Эквивалент Java метода getReturnTypeDefinition()
     */
    fun getReturnTypeDefinition(): TypeDefinition {
        return TypeDefinition(returnType, "Возвращаемое значение")
    }

    /**
     * Вспомогательный data class для типа возврата
     *
     * Заменяет вложенный Java record TypeDefinition
     */
    data class TypeDefinition(
        val name: String,
        val description: String
    )
} 