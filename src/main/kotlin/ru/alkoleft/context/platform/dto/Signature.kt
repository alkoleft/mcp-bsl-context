package ru.alkoleft.context.platform.dto

/**
 * Kotlin data class для сигнатур методов
 *
 * Заменяет Java record Signature с реализацией интерфейса ISignature
 */
data class Signature(
    override val name: String,
    override val description: String,
    override val params: List<ParameterDefinition>
) : ISignature 