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
    CONSTRUCTOR;

    /**
     * Преобразование в строковое представление для совместимости с Java API
     */
    fun toStringType(): String = when (this) {
        METHOD -> "method"
        PROPERTY -> "property"
        TYPE -> "type"
        CONSTRUCTOR -> "constructor"
    }

    companion object {
        /**
         * Создание ApiType из строки для обратной совместимости
         */
        fun fromString(type: String?): ApiType? = when (type?.lowercase()) {
            "method" -> METHOD
            "property" -> PROPERTY
            "type" -> TYPE
            "constructor" -> CONSTRUCTOR
            else -> null
        }
    }
} 