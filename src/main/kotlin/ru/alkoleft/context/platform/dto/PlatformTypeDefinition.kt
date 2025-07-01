package ru.alkoleft.context.platform.dto

/**
 * Kotlin data class для определения платформенных типов 1С Предприятие
 *
 * Заменяет Java record PlatformTypeDefinition с полной функциональной совместимостью
 */
data class PlatformTypeDefinition(
    val name: String,
    val description: String,
    val methods: List<MethodDefinition>,
    val properties: List<PropertyDefinition>,
    val constructors: List<ISignature>
) 