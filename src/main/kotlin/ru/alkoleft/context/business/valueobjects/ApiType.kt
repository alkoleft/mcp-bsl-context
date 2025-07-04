/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.valueobjects

/**
 * Value Object: Типы API элементов платформы 1С Предприятие
 */
enum class ApiType {
    METHOD,
    PROPERTY,
    TYPE,
    CONSTRUCTOR,
    ;

    /**
     * Получение отображаемого имени
     */
    fun getDisplayName(): String =
        when (this) {
            METHOD -> "Метод"
            PROPERTY -> "Свойство"
            TYPE -> "Тип"
            CONSTRUCTOR -> "Конструктор"
        }

    /**
     * Получение множественного числа
     */
    fun getPluralName(): String =
        when (this) {
            METHOD -> "Методы"
            PROPERTY -> "Свойства"
            TYPE -> "Типы"
            CONSTRUCTOR -> "Конструкторы"
        }

    companion object {
        fun getType(type: String) =
            when (type.lowercase()) {
                "object",
                "class",
                "datatype",
                "объект",
                "класс",
                "тип",
                "структура",
                "данные",
                "type",
                -> TYPE

                "метод",
                "функция",
                "процедура",
                "method",
                -> METHOD

                "свойство",
                "реквизит",
                "поле",
                "атрибут",
                "property",
                -> PROPERTY

                else -> null
            }
    }
}
