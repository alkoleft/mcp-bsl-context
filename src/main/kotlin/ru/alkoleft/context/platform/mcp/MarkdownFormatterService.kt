/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.mcp

import org.springframework.stereotype.Service
import ru.alkoleft.context.platform.dto.ISignature
import ru.alkoleft.context.platform.dto.MethodDefinition
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import ru.alkoleft.context.platform.dto.PropertyDefinition
import ru.alkoleft.context.platform.dto.Signature

/**
 * Kotlin реализация улучшенного сервиса форматирования результатов поиска в Markdown для MCP
 *
 * Основные улучшения:
 * - Типобезопасность с sealed classes
 * - Функциональное программирование
 * - String templates с улучшенной читаемостью
 * - Extension functions для работы с DTO
 * - Null safety
 */
@Service
class MarkdownFormatterService {
    /**
     * Форматирование результатов поиска
     */
    fun formatSearchResults(
        query: String,
        results: List<Any>,
    ): String {
        if (results.isEmpty()) {
            return formatEmptyResults(query)
        }

        return buildString {
            appendLine("# 🔎 Результаты поиска: \"$query\" (${results.size} найдено)")
            appendLine()

            when {
                results.size == 1 -> {
                    // Один результат - детальное описание
                    append(formatSingleObject(results.first()))
                }

                results.size <= 5 -> {
                    // Несколько результатов - краткое описание каждого
                    results.forEachIndexed { index, result ->
                        append(formatCompactObject(result, index == 0))
                        if (index < results.size - 1) {
                            appendLine("\n---\n")
                        }
                    }
                }

                else -> {
                    // Много результатов - табличный формат для топ-5
                    appendLine("## Топ результаты\n")
                    appendLine("| Название | Тип | Сигнатура |")
                    appendLine("|----------|-----|-----------|")

                    results.forEach { result ->
                        appendLine("| **${result.objectName}** | ${result.typeIcon} | `${result.signature.truncate(40)}` |")
                    }

                    // Детальное описание первого результата
                    appendLine("\n---\n")
                    appendLine("## ⭐ Наиболее релевантный результат\n")
                    append(formatSingleObject(results.first()))
                }
            }
        }
    }

    /**
     * Форматирование детальной информации об элементе
     */
    fun formatDetailedInfo(obj: Any): String =
        when (obj) {
            is MethodDefinition -> formatMethodDefinition(obj)
            is PropertyDefinition -> formatPropertyDefinition(obj)
            is PlatformTypeDefinition -> formatPlatformTypeDefinition(obj)
            else -> "❌ **Неподдерживаемый тип объекта:** ${obj::class.simpleName}"
        }

    /**
     * Форматирование конструкторов типа
     */
    fun formatConstructors(
        constructors: List<ISignature>,
        typeName: String,
    ): String =
        buildString {
            appendLine("# 🔨 Конструкторы типа $typeName (${constructors.size} найдено)\n")

            constructors.forEachIndexed { index, constructor ->
                appendLine("## Конструктор ${index + 1}")
                appendLine("```bsl")
                val params = constructor.params.joinToString(", ") { "${it.name}: ${it.type}" }
                appendLine("Новый $typeName($params)")
                appendLine("```\n")

                constructor.description.takeIf { it.isNotBlank() }?.let {
                    appendLine("**Описание:** $it\n")
                }

                if (constructor.params.isNotEmpty()) {
                    appendLine("**Параметры:**")
                    constructor.params.forEach { param ->
                        val requiredMark = if (param.required) "(обязательный)" else ""
                        val description = param.description
                        appendLine("- **${param.name}** *(${param.type})* $requiredMark - $description")
                    }
                    appendLine()
                }
            }
        }

    /**
     * Форматирование всех элементов типа
     */
    fun formatTypeMembers(type: PlatformTypeDefinition): String =
        buildString {
            appendLine("# 📦 Элементы типа ${type.name}\n")

            // Методы
            if (type.methods.isNotEmpty()) {
                appendLine("## 🔧 Методы (${type.methods.size})\n")
                type.methods.forEach { method ->
                    val signature = method.buildMethodSignature()
                    val description = method.description
                    appendLine("- **$signature** - $description")
                }
                appendLine()
            }

            // Свойства
            if (type.properties.isNotEmpty()) {
                appendLine("## 📋 Свойства (${type.properties.size})\n")
                type.properties.forEach { property ->
                    val description = property.description
                    appendLine("- **${property.name}** *(${property.type})* - $description")
                }
                appendLine()
            }

            // Конструкторы
            if (type.constructors.isNotEmpty()) {
                appendLine("## 🔨 Конструкторы (${type.constructors.size})\n")
                appendLine("*Для получения детальной информации о конструкторах используйте getConstructors*\n")
            }
        }

    // Приватные методы форматирования

    private fun formatEmptyResults(query: String): String =
        """
        ❌ **Ничего не найдено по запросу:** `$query`

        💡 **Попробуйте:**
        - Проверить правописание
        - Использовать более короткий запрос
        - Попробовать синонимы
        """.trimIndent()

    private fun formatMethodDefinition(method: MethodDefinition): String =
        buildString {
            appendLine("# 🔧 ${method.name}\n")

            // Сигнатуры
            method.signature.forEach { signature ->
                appendLine("## Сигнатура: ${signature.name} (${signature.description})")
                appendLine("```bsl")
                appendLine(method.buildMethodSignature(signature))
                appendLine("```\n")

                // Параметры
                if (signature.params.isNotEmpty()) {
                    appendLine("### Параметры")
                    signature.params.forEach { param ->
                        val requiredMark = if (param.required) "(обязательный)" else ""
                        val description = param.description
                        appendLine("- **${param.name}** *(${param.type})* $requiredMark - $description")
                    }
                    appendLine()
                }
            }

            // Возвращаемое значение
            method.returnType.let { returnType ->
                appendLine("## Возвращаемое значение")
                val returnTypeDef = method.getReturnTypeDefinition()
                val description = returnTypeDef.description
                appendLine("**${returnTypeDef.name}** - $description\n")
            }

            // Описание метода
            method.description.takeIf { it.isNotBlank() }?.let {
                appendLine("## Описание")
                appendLine(it)
            }
        }

    private fun formatPropertyDefinition(property: PropertyDefinition): String =
        buildString {
            appendLine("# 📋 ${property.name}\n")

            appendLine("**Тип:** ${property.type}")
            appendLine("**Доступность:** ${if (property.readonly) "Только чтение" else "Чтение/запись"}")
            appendLine()

            property.description.takeIf { it.isNotBlank() }?.let {
                appendLine("## Описание")
                appendLine(it)
            }
        }

    private fun formatPlatformTypeDefinition(type: PlatformTypeDefinition): String =
        buildString {
            appendLine("# 📦 ${type.name}\n")

            type.description.takeIf { it.isNotBlank() }?.let {
                appendLine("## Описание")
                appendLine("$it\n")
            }

            // Краткая статистика
            appendLine("## Статистика")
            appendLine("- **Методы:** ${type.methods.size}")
            appendLine("- **Свойства:** ${type.properties.size}")
            appendLine("- **Конструкторы:** ${type.constructors.size}")
            appendLine()

            // Основные методы (топ-5)
            if (type.methods.isNotEmpty()) {
                appendLine("## Основные методы")
                type.methods.take(5).forEach { method ->
                    val signature = method.buildMethodSignature()
                    appendLine("- `$signature`")
                }
                if (type.methods.size > 5) {
                    appendLine("- *...и ещё ${type.methods.size - 5} методов*")
                }
                appendLine()
            }

            // Основные свойства (топ-5)
            if (type.properties.isNotEmpty()) {
                appendLine("## Основные свойства")
                type.properties.take(5).forEach { property ->
                    appendLine("- **${property.name}** *(${property.type})*")
                }
                if (type.properties.size > 5) {
                    appendLine("- *...и ещё ${type.properties.size - 5} свойств*")
                }
                appendLine()
            }

            appendLine("💡 *Используйте getMembers для получения полного списка*")
        }

    private fun formatSingleObject(obj: Any): String =
        when (obj) {
            is MethodDefinition -> formatMethodDefinition(obj)
            is PropertyDefinition -> formatPropertyDefinition(obj)
            is PlatformTypeDefinition -> formatPlatformTypeDefinition(obj)
            else -> "❌ **Неподдерживаемый тип объекта:** ${obj::class.simpleName}"
        }

    private fun formatCompactObject(
        obj: Any,
        isFirst: Boolean,
    ): String =
        buildString {
            val prefix = if (isFirst) "⭐" else "•"

            when (obj) {
                is MethodDefinition -> {
                    appendLine("$prefix **${obj.name}** (Метод)")
                    appendLine("   - Сигнатура: `${obj.buildMethodSignature()}`")
                    obj.description.takeIf { it.isNotBlank() }?.let {
                        appendLine("   - Описание: $it")
                    }
                }

                is PropertyDefinition -> {
                    appendLine("$prefix **${obj.name}** (Свойство)")
                    appendLine("   - Тип: `${obj.type}`")
                    appendLine("   - Доступность: ${if (obj.readonly) "Только чтение" else "Чтение/запись"}")
                    obj.description.takeIf { it.isNotBlank() }?.let {
                        appendLine("   - Описание: $it")
                    }
                }

                is PlatformTypeDefinition -> {
                    appendLine("$prefix **${obj.name}** (Тип)")
                    appendLine("   - Методы: ${obj.methods.size}, Свойства: ${obj.properties.size}")
                    obj.description.takeIf { it.isNotBlank() }?.let {
                        appendLine("   - Описание: $it")
                    }
                }
            }
        }

    // Extension functions для удобства

    private val Any.objectName: String
        get() =
            when (this) {
                is MethodDefinition -> name
                is PropertyDefinition -> name
                is PlatformTypeDefinition -> name
                else -> toString()
            }

    private val Any.typeIcon: String
        get() =
            when (this) {
                is MethodDefinition -> "🔧 Метод"
                is PropertyDefinition -> "📋 Свойство"
                is PlatformTypeDefinition -> "📦 Тип"
                else -> "❓ Неизвестно"
            }

    private val Any.signature: String
        get() =
            when (this) {
                is MethodDefinition -> buildMethodSignature()
                is PropertyDefinition -> "$name: $type"
                is PlatformTypeDefinition -> name
                else -> toString()
            }

    private fun String.truncate(maxLength: Int): String = if (length <= maxLength) this else "${take(maxLength - 3)}..."

    private fun MethodDefinition.buildMethodSignature(): String {
        val firstSignature = signature.firstOrNull() ?: return name
        return buildMethodSignature(firstSignature)
    }

    private fun MethodDefinition.buildMethodSignature(signature: Signature): String {
        val params =
            signature.params.joinToString(", ") { param ->
                "${param.name}: ${param.type}"
            }
        return "$name($params): $returnType"
    }
}
