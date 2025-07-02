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
 * Markdown форматтер для результатов поиска
 * Самостоятельная реализация без зависимостей
 */
@Component
class McpMarkdownFormatter : ResultFormatter {
    override val formatType = FormatType.MARKDOWN

    override fun getMimeType(): String = "text/markdown"

    /**
     * Форматирование результатов поиска в Markdown
     */
    override suspend fun formatSearchResults(result: SearchResult): String {
        return buildString {
            appendLine("# 🔍 Результаты поиска: \"${result.query.text}\"")
            appendLine()

            if (result.items.isEmpty()) {
                appendLine("❌ **Ничего не найдено**")
                appendLine()
                appendLine("💡 Попробуйте:")
                appendLine("- Проверить правописание")
                appendLine("- Использовать более общие термины")
                appendLine("- Попробовать синонимы")
                return@buildString
            }

            appendLine("📊 **Статистика поиска:**")
            appendLine("- Найдено результатов: **${result.items.size}**")
            appendLine("- Время выполнения: **${result.executionTimeMs}мс**")
            appendLine("- Алгоритм: **${result.algorithm.name}**")
            appendLine()

            result.items.forEachIndexed { index, item ->
                val element = item.element
                appendLine("## ${index + 1}. ${formatElementHeader(element)}")

                // Показываем релевантность
                val relevancePercent = (item.relevanceScore * 100).toInt()
                appendLine("**Релевантность:** $relevancePercent% | **Причина:** ${formatMatchReason(item.matchReason)}")

                // Показываем описание если есть
                if (element.description.isNotBlank()) {
                    appendLine()
                    appendLine("📝 ${element.description}")
                }

                // Показываем подсвеченный текст если есть
                item.highlightedText?.let { highlighted ->
                    appendLine()
                    appendLine("🔍 **Совпадение:** $highlighted")
                }

                appendLine()
                appendLine("---")
                appendLine()
            }
        }
    }

    /**
     * Форматирование детальной информации об элементе
     */
    override suspend fun formatDetailedInfo(element: ApiElement): String =
        buildString {
            appendLine("# 📋 ${formatElementHeader(element)}")
            appendLine()

            appendLine("**Источник:** ${element.source.name}")
            appendLine()

            if (element.description.isNotBlank()) {
                appendLine("## 📝 Описание")
                appendLine(element.description)
                appendLine()
            }

            when (element) {
                is ru.alkoleft.context.core.domain.api.Method -> {
                    appendLine("## 🔧 Детали метода")
                    appendLine("- **Глобальный:** ${if (element.isGlobal) "Да" else "Нет"}")
                    element.parentType?.let {
                        appendLine("- **Родительский тип:** ${it.name}")
                    }
                    element.returnType?.let {
                        appendLine("- **Тип возврата:** ${it.name}")
                    }

                    if (element.signatures.isNotEmpty()) {
                        appendLine()
                        appendLine("### 📋 Сигнатуры (${element.signatures.size})")
                        element.signatures.forEachIndexed { index, signature ->
                            appendLine("#### Сигнатура ${index + 1}")
                            if (signature.parameters.isNotEmpty()) {
                                appendLine("**Параметры:**")
                                signature.parameters.forEach { param ->
                                    val optional = if (param.isOptional) " *(опционально)*" else ""
                                    appendLine("- `${param.name}`: ${param.type.name}$optional")
                                    if (param.description.isNotBlank()) {
                                        appendLine("  - ${param.description}")
                                    }
                                }
                            }
                            signature.returnType?.let {
                                appendLine("**Возвращает:** ${it.name}")
                            }
                        }
                    }
                }

                is ru.alkoleft.context.core.domain.api.Property -> {
                    appendLine("## 📋 Детали свойства")
                    appendLine("- **Тип данных:** ${element.dataType.name}")
                    appendLine("- **Только чтение:** ${if (element.isReadonly) "Да" else "Нет"}")
                    element.parentType?.let {
                        appendLine("- **Родительский тип:** ${it.name}")
                    }
                }

                is ru.alkoleft.context.core.domain.api.Type -> {
                    appendLine("## 🏷️ Детали типа")
                    appendLine("- **Методов:** ${element.methods.size}")
                    appendLine("- **Свойств:** ${element.properties.size}")
                    appendLine("- **Конструкторов:** ${element.constructors.size}")
                }

                is ru.alkoleft.context.core.domain.api.Constructor -> {
                    appendLine("## 🏗️ Детали конструктора")
                    appendLine("- **Родительский тип:** ${element.parentType.name}")
                    if (element.parameters.isNotEmpty()) {
                        appendLine()
                        appendLine("### 📋 Параметры")
                        element.parameters.forEach { param ->
                            val optional = if (param.isOptional) " *(опционально)*" else ""
                            appendLine("- `${param.name}`: ${param.type.name}$optional")
                            if (param.description.isNotBlank()) {
                                appendLine("  - ${param.description}")
                            }
                        }
                    }
                }
            }
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
                appendLine("# $it")
                appendLine()
            }

            if (elements.isEmpty()) {
                appendLine("❌ **Элементы не найдены**")
                return@buildString
            }

            appendLine("📋 **Всего элементов:** ${elements.size}")
            appendLine()

            val groupedElements =
                elements.groupBy { element ->
                    when (element) {
                        is ru.alkoleft.context.core.domain.api.Method -> "🔧 Методы"
                        is ru.alkoleft.context.core.domain.api.Property -> "📋 Свойства"
                        is ru.alkoleft.context.core.domain.api.Type -> "🏷️ Типы"
                        is ru.alkoleft.context.core.domain.api.Constructor -> "🏗️ Конструкторы"
                    }
                }

            groupedElements.forEach { (category, elementList) ->
                appendLine("## $category (${elementList.size})")
                appendLine()

                elementList.take(10).forEach { element ->
                    appendLine("- **${element.name}**")
                    if (element.description.isNotBlank()) {
                        appendLine("  - ${element.description}")
                    }
                }

                if (elementList.size > 10) {
                    appendLine("- *...и ещё ${elementList.size - 10} элементов*")
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
            is ru.alkoleft.context.core.domain.api.Method -> "🔧 ${element.name}()"
            is ru.alkoleft.context.core.domain.api.Property -> "📋 ${element.name}"
            is ru.alkoleft.context.core.domain.api.Type -> "🏷️ ${element.name}"
            is ru.alkoleft.context.core.domain.api.Constructor -> "🏗️ ${element.name}"
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
            is ru.alkoleft.context.core.domain.search.MatchReason.MultipleMatches -> {
                "Множественные совпадения (${matchReason.reasons.size})"
            }
        }
}
