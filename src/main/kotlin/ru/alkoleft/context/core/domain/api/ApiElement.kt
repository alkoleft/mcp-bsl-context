/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.domain.api

/**
 * Базовая абстракция для всех элементов API платформы 1С
 * Sealed class обеспечивает type safety и exhaustive when
 */
sealed class ApiElement {
    abstract val name: String
    abstract val description: String
    abstract val source: DataSource

    /**
     * Уникальный идентификатор элемента для индексирования
     */
    val id: String get() = "${javaClass.simpleName.lowercase()}_${name.lowercase()}"
}

/**
 * Метод API платформы
 */
data class Method(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val signatures: List<MethodSignature>,
    val returnType: TypeReference?,
    val isGlobal: Boolean = false,
    val parentType: TypeReference? = null,
) : ApiElement()

/**
 * Свойство API платформы
 */
data class Property(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val dataType: TypeReference,
    val isReadonly: Boolean,
    val parentType: TypeReference? = null,
) : ApiElement()

/**
 * Тип данных платформы
 */
data class Type(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val methods: List<Method>,
    val properties: List<Property>,
    val constructors: List<Constructor>,
    val baseTypes: List<TypeReference> = emptyList(),
) : ApiElement()

/**
 * Конструктор типа
 */
data class Constructor(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val parameters: List<Parameter>,
    val parentType: TypeReference,
) : ApiElement()

/**
 * Сигнатура метода
 */
data class MethodSignature(
    val parameters: List<Parameter>,
    val returnType: TypeReference?,
    val isDeprecated: Boolean = false,
)

/**
 * Параметр метода или конструктора
 */
data class Parameter(
    val name: String,
    val type: TypeReference,
    val isOptional: Boolean = false,
    val defaultValue: String? = null,
    val description: String = "",
)

/**
 * Ссылка на тип данных
 */
data class TypeReference(
    val name: String,
    val fullName: String = name,
    val isArray: Boolean = false,
    val isNullable: Boolean = false,
)

/**
 * Источник данных элемента API
 */
enum class DataSource {
    BSL_CONTEXT, // Текущий источник
    CONFIGURATION, // Будущий источник - метаданные конфигурации
    EXTENSION, // Будущий источник - расширения
    DOCUMENTATION, // Будущий источник - документация
}
