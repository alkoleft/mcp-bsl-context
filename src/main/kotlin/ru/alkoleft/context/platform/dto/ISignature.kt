package ru.alkoleft.context.platform.dto

/**
 * Kotlin интерфейс для сигнатур методов и конструкторов
 *
 * Заменяет Java интерфейс ISignature с полной функциональной совместимостью
 */
interface ISignature {
    val name: String
    val description: String
    val params: List<ParameterDefinition>
} 