/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.formatters

import org.springframework.stereotype.Service
import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition
import ru.alkoleft.context.business.entities.Signature
import ru.alkoleft.context.business.services.ResponseFormatterService
import ru.alkoleft.context.exceptions.DomainException

/**
 * Сервис форматирования в Markdown
 *
 * Форматирует результаты поиска и детальную информацию в читаемый Markdown
 */
@Service
class MarkdownFormatterService : ResponseFormatterService {
    override fun formatError(e: Throwable) =
        if (e is DomainException) "❌ ${e.message}" else "❌ **Ошибка:** ${e.message}"

    override fun formatQuery(query: String) = "# Результаты поиска: '${query}'\n\n"

    /**
     * Форматирует результаты поиска в Markdown
     */
    override fun formatSearchResults(result: List<Definition>) = formatDefinitions(result)

    override fun formatInfo(definition: Definition?) =
        when (definition) {
            is PlatformTypeDefinition -> formatPlatformType(definition)
            is PropertyDefinition -> formatProperty(definition)
            is MethodDefinition -> formatMethod(definition)
            null -> "❌ **Не найдено:** Ничего не найдено для запроса\n"
        }

    override fun formatDefinitions(definitions: List<Definition>): String {
        val builder = StringBuilder()
        formatSearchResults(builder, definitions)
        return builder.toString()

    }

    override fun formatConstructors(result: List<Signature>): String {
        TODO("Not yet implemented")
    }

    private fun formatSearchResults(
        builder: StringBuilder,
        results: List<Definition>,
    ): String {
        if (results.isEmpty()) {
            builder.append("❌ **Не найдено:** Ничего не найдено для запроса\n")
            return builder.toString()
        }

        builder.append("**Найдено результатов:** ${results.size}\n\n")

        val methods = results.filterIsInstance<MethodDefinition>()
        val properties = results.filterIsInstance<PropertyDefinition>()
        val types = results.filterIsInstance<PlatformTypeDefinition>()
        // Методы
        if (methods.isNotEmpty()) {
            builder.append("## Методы\n\n")
            methods.forEach { method ->
                builder.append(formatMethodSummary(method))
            }
        }

        // Свойства
        if (properties.isNotEmpty()) {
            builder.append("## Свойства\n\n")
            properties.forEach { property ->
                builder.append(formatPropertySummary(property))
            }
        }

        // Типы
        if (types.isNotEmpty()) {
            builder.append("## Типы\n\n")
            types.forEach { type ->
                builder.append(formatTypeSummary(type))
            }
        }

        return builder.toString()
    }

    /**
     * Форматирует тип платформы в Markdown
     */
    fun formatPlatformType(type: PlatformTypeDefinition): String {
        val builder = StringBuilder()

        // Заголовок типа
        builder.append("# ${type.name}\n\n")

        // Методы
        if (type.hasMethods()) {
            builder.append("## Методы\n\n")
            type.methods.forEach { method ->
                builder.append(formatMethod(method))
            }
        }

        // Свойства
        if (type.hasProperties()) {
            builder.append("## Свойства\n\n")
            type.properties.forEach { property ->
                builder.append(formatProperty(property))
            }
        }

        return builder.toString()
    }

    /**
     * Форматирует метод в Markdown (публичный метод)
     */
    fun formatMethod(method: MethodDefinition): String {
        val builder = StringBuilder()

        builder.append("### ${method.name}\n\n")

        if (method.description.isNotBlank()) {
            builder.append("${method.description}\n\n")
        }

        builder.append("**Возвращаемый тип:** `${method.returnType}`\n\n")

//        if (method.parameters.isNotEmpty()) {
//            builder.append("**Параметры:**\n\n")
//            method.parameters.forEach { param ->
//                builder.append("- `${param.name}: ${param.type}` - ${param.description}\n")
//            }
//            builder.append("\n")
//        }

        return builder.toString()
    }

    /**
     * Форматирует свойство в Markdown (публичный метод)
     */
    fun formatProperty(property: PropertyDefinition): String {
        val builder = StringBuilder()

        builder.append("### ${property.name}\n\n")

        if (property.description.isNotBlank()) {
            builder.append("${property.description}\n\n")
        }

        builder.append("**Тип:** `${property.propertyType}`\n")
        builder.append("**Только для чтения:** ${if (property.isReadOnly) "Да" else "Нет"}\n\n")

        return builder.toString()
    }

    /**
     * Форматирует краткую информацию о методе
     */
    private fun formatMethodSummary(method: MethodDefinition): String = "- **${method.name}** - ${method.description}\n"

    /**
     * Форматирует краткую информацию о свойстве
     */
    private fun formatPropertySummary(property: PropertyDefinition): String =
        "- **${property.name}** (${property.propertyType}) - ${property.description}\n"

    /**
     * Форматирует краткую информацию о типе
     */
    private fun formatTypeSummary(type: PlatformTypeDefinition): String =
        "- **${type.name}** - ${type.methods.size} методов, ${type.properties.size} свойств\n"

    /**
     * Форматирует детальную информацию об элементе API
     */
    fun formatDetailedInfo(element: Any): String =
        when (element) {
            is PlatformTypeDefinition -> formatPlatformType(element)
            is MethodDefinition -> formatMethod(element)
            is PropertyDefinition -> formatProperty(element)
            else -> "❌ **Ошибка:** Неизвестный тип элемента"
        }

    /**
     * Форматирует информацию о члене типа
     */
    fun formatMemberInfo(
        typeName: String,
        member: Any,
    ): String {
        val builder = StringBuilder()

        builder.append(
            "# $typeName.${
                when (member) {
                    is MethodDefinition -> member.name
                    is PropertyDefinition -> member.name
                    else -> "Unknown"
                }
            }\n\n",
        )

        builder.append("**Тип:** ${typeName}\n\n")

        when (member) {
            is MethodDefinition -> builder.append(formatMethod(member))
            is PropertyDefinition -> builder.append(formatProperty(member))
            else -> builder.append("❌ **Ошибка:** Неизвестный тип элемента")
        }

        return builder.toString()
    }
}
