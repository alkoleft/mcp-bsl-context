/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.formatters

import org.springframework.stereotype.Component
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter

/**
 * Простой текстовый форматтер для результатов поиска
 * Подходит для консольного вывода и интеграции с простыми системами
 */
@Component
class PlainTextFormatter : ResultFormatter {
    override val formatType = FormatType.PLAIN_TEXT

    override fun getMimeType(): String = "text/plain"

    companion object {
        private const val SEPARATOR = "============================================================"
        private const val SUB_SEPARATOR = "----------------------------------------"
    }

    /**
     * Форматирование результатов поиска в простой текст
     */
    override suspend fun formatSearchResults(result: SearchResult): String {
        return buildString {
            appendLine("РЕЗУЛЬТАТЫ ПОИСКА: \"${result.query.text}\"")
            appendLine(SEPARATOR)
            appendLine()

            if (result.items.isEmpty()) {
                appendLine("НИЧЕГО НЕ НАЙДЕНО")
                appendLine()
                appendLine("Попробуйте:")
                appendLine("- Проверить правописание")
                appendLine("- Использовать более общие термины")
                appendLine("- Попробовать синонимы")
                return@buildString
            }

            appendLine("СТАТИСТИКА:")
            appendLine("Найдено результатов: ${result.items.size}")
            appendLine("Время выполнения: ${result.executionTimeMs}мс")
            appendLine("Алгоритм: ${result.algorithm.name}")
            appendLine()
            appendLine(SEPARATOR)
            appendLine()

            result.items.forEachIndexed { index, item ->
                val element = item.element
                appendLine("${index + 1}. ${formatElementHeader(element)}")
                appendLine(SUB_SEPARATOR)

                // Релевантность и причина
                val relevancePercent = (item.relevanceScore * 100).toInt()
                appendLine("Релевантность: $relevancePercent%")
                appendLine("Причина: ${formatMatchReason(item.matchReason)}")

                // Описание
                if (element.description.isNotBlank()) {
                    appendLine()
                    appendLine("Описание:")
                    appendLine(element.description)
                }

                // Дополнительная информация
                appendLine()
                appendLine("Детали:")
                appendLine(formatElementDetails(element))

                // Подсвеченный текст
                item.highlightedText?.let { highlighted ->
                    appendLine()
                    appendLine("Совпадение: $highlighted")
                }

                appendLine()
                appendLine(SUB_SEPARATOR)
                appendLine()
            }
        }
    }

    /**
     * Форматирование детальной информации об элементе
     */
    override suspend fun formatDetailedInfo(element: ApiElement): String =
        buildString {
            appendLine("ДЕТАЛЬНАЯ ИНФОРМАЦИЯ: ${element.name}")
            appendLine(SEPARATOR)
            appendLine()

            appendLine("Тип: ${formatElementType(element)}")
            appendLine("Источник: ${element.source.name}")

            if (element.description.isNotBlank()) {
                appendLine()
                appendLine("Описание:")
                appendLine(element.description)
            }

            appendLine()
            appendLine(formatElementDetails(element))
        }

    /**
     * Форматирование списка элементов
     */
    override suspend fun formatElementList(
        elements: List<ApiElement>,
        title: String?,
    ): String {
        return buildString {
            title?.let {
                appendLine(it.uppercase())
                appendLine(SEPARATOR)
                appendLine()
            }

            if (elements.isEmpty()) {
                appendLine("ЭЛЕМЕНТЫ НЕ НАЙДЕНЫ")
                return@buildString
            }

            appendLine("ВСЕГО ЭЛЕМЕНТОВ: ${elements.size}")
            appendLine()

            val groupedElements =
                elements.groupBy { element ->
                    when (element) {
                        is ru.alkoleft.context.core.domain.api.Method -> "МЕТОДЫ"
                        is ru.alkoleft.context.core.domain.api.Property -> "СВОЙСТВА"
                        is ru.alkoleft.context.core.domain.api.Type -> "ТИПЫ"
                        is ru.alkoleft.context.core.domain.api.Constructor -> "КОНСТРУКТОРЫ"
                    }
                }

            groupedElements.forEach { (category, elementList) ->
                appendLine("$category (${elementList.size}):")
                appendLine(SUB_SEPARATOR)

                elementList.take(20).forEach { element ->
                    appendLine("- ${element.name}")
                    if (element.description.isNotBlank()) {
                        appendLine("  ${element.description}")
                    }
                }

                if (elementList.size > 20) {
                    appendLine("...и ещё ${elementList.size - 20} элементов")
                }

                appendLine()
            }
        }
    }

    /**
     * Форматирование заголовка элемента
     */
    private fun formatElementHeader(element: ApiElement): String =
        when (element) {
            is ru.alkoleft.context.core.domain.api.Method -> "${element.name}() [МЕТОД]"
            is ru.alkoleft.context.core.domain.api.Property -> "${element.name} [СВОЙСТВО]"
            is ru.alkoleft.context.core.domain.api.Type -> "${element.name} [ТИП]"
            is ru.alkoleft.context.core.domain.api.Constructor -> "${element.name} [КОНСТРУКТОР]"
        }

    /**
     * Форматирование типа элемента
     */
    private fun formatElementType(element: ApiElement): String =
        when (element) {
            is ru.alkoleft.context.core.domain.api.Method -> "Метод"
            is ru.alkoleft.context.core.domain.api.Property -> "Свойство"
            is ru.alkoleft.context.core.domain.api.Type -> "Тип"
            is ru.alkoleft.context.core.domain.api.Constructor -> "Конструктор"
        }

    /**
     * Форматирование деталей элемента
     */
    private fun formatElementDetails(element: ApiElement): String =
        buildString {
            when (element) {
                is ru.alkoleft.context.core.domain.api.Method -> {
                    appendLine("- Глобальный: ${if (element.isGlobal) "Да" else "Нет"}")
                    element.parentType?.let {
                        appendLine("- Родительский тип: ${it.name}")
                    }
                    element.returnType?.let {
                        appendLine("- Тип возврата: ${it.name}")
                    }

                    if (element.signatures.isNotEmpty()) {
                        appendLine("- Сигнатуры: ${element.signatures.size}")
                        element.signatures.forEach { signature ->
                            val params =
                                signature.parameters.joinToString(", ") { param ->
                                    "${param.name}: ${param.type.name}${if (param.isOptional) " (опционально)" else ""}"
                                }
                            appendLine("  ($params)")
                        }
                    }
                }

                is ru.alkoleft.context.core.domain.api.Property -> {
                    appendLine("- Тип данных: ${element.dataType.name}")
                    appendLine("- Только чтение: ${if (element.isReadonly) "Да" else "Нет"}")
                    element.parentType?.let {
                        appendLine("- Родительский тип: ${it.name}")
                    }
                }

                is ru.alkoleft.context.core.domain.api.Type -> {
                    appendLine("- Методов: ${element.methods.size}")
                    appendLine("- Свойств: ${element.properties.size}")
                    appendLine("- Конструкторов: ${element.constructors.size}")
                }

                is ru.alkoleft.context.core.domain.api.Constructor -> {
                    appendLine("- Родительский тип: ${element.parentType.name}")
                    if (element.parameters.isNotEmpty()) {
                        appendLine("- Параметры:")
                        element.parameters.forEach { param ->
                            appendLine("  - ${param.name}: ${param.type.name}${if (param.isOptional) " (опционально)" else ""}")
                        }
                    }
                }
            }
        }

    /**
     * Форматирование причины совпадения
     */
    private fun formatMatchReason(matchReason: ru.alkoleft.context.core.domain.search.MatchReason): String =
        when (matchReason) {
            is ru.alkoleft.context.core.domain.search.MatchReason.ExactMatch ->
                "Точное совпадение в ${matchReason.field}"
            is ru.alkoleft.context.core.domain.search.MatchReason.PrefixMatch ->
                "Совпадение префикса в ${matchReason.field}"
            is ru.alkoleft.context.core.domain.search.MatchReason.ContainsMatch ->
                "Содержит текст в ${matchReason.field}"
            is ru.alkoleft.context.core.domain.search.MatchReason.FuzzyMatch ->
                "Нечеткое совпадение в ${matchReason.field} (${(matchReason.similarity * 100).toInt()}%)"
            is ru.alkoleft.context.core.domain.search.MatchReason.SemanticMatch ->
                "Семантическое совпадение (${(matchReason.similarity * 100).toInt()}%)"
            is ru.alkoleft.context.core.domain.search.MatchReason.MultipleMatches ->
                "Множественные совпадения (${matchReason.reasons.size})"
        }
}
