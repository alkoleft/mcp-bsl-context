/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.formatters

import org.springframework.stereotype.Component
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.api.Constructor
import ru.alkoleft.context.core.domain.api.Method
import ru.alkoleft.context.core.domain.api.Property
import ru.alkoleft.context.core.domain.api.Type
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter

/**
 * Markdown форматтер для результатов поиска
 * Самостоятельная реализация без зависимостей
 */
@Component
class MarkdownFormatter : ResultFormatter {
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

            result.items.forEachIndexed { index, item ->
                val element = item.element
                appendLine("## ${index + 1}. ${formatElementHeader(element)}")

                // Показываем описание если есть
                if (element.description.isNotBlank()) {
                    appendLine()
                    appendLine("📝 ${element.description}")
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
                is Method -> {
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

                is Property -> {
                    appendLine("## 📋 Детали свойства")
                    appendLine("- **Тип данных:** ${element.dataType.name}")
                    appendLine("- **Только чтение:** ${if (element.isReadonly) "Да" else "Нет"}")
                    element.parentType?.let {
                        appendLine("- **Родительский тип:** ${it.name}")
                    }
                }

                is Type -> {
                    appendLine("## 🏷️ Детали типа")
                    appendLine("- **Методов:** ${element.methods.size}")
                    appendLine("- **Свойств:** ${element.properties.size}")
                    appendLine("- **Конструкторов:** ${element.constructors.size}")
                }

                is Constructor -> {
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
                        is Method -> "🔧 Методы"
                        is Property -> "📋 Свойства"
                        is Type -> "🏷️ Типы"
                        is Constructor -> "🏗️ Конструкторы"
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
            is Method -> "🔧 ${element.name}()"
            is Property -> "📋 ${element.name}"
            is Type -> "🏷️ ${element.name}"
            is Constructor -> "🏗️ ${element.name}"
        }
}
