/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.pages.PageParser
import java.io.InputStream

/**
 * Информация о глобальном свойстве
 */
data class RelatedObject(
    val name: String,
    val href: String
)

data class PropertyInfo(
    val propertyNameRu: String,
    val propertyNameEn: String,
    val description: String,
    val readonly: Boolean,
    val typeName: String,
    val relatedObjects: List<RelatedObject>
)

enum class BlockType {
    NONE, NAME, USAGE, DESCRIPTION, RELATED, TYPE
}

class PropertyPageParser : PageParser<PropertyInfo>() {
    override fun parse(inputStream: InputStream): PropertyInfo {
        var propertyNameRu = ""
        var propertyNameEn = ""
        val description = StringBuilder()
        var readonly = false
        var typeNames = ""
        val relatedObjects = mutableListOf<RelatedObject>()

        var block = BlockType.NONE
        var currentRelatedHref = ""

        // Создаем кастомное правило для PropertyPageParser
        val markdownHandler = MarkdownHtmlHandler.builder()
            .onOpenTag { name, attributes, _, _ ->
                when {
                    name == "p" && attributes["class"] == "V8SH_heading" -> {
                        block = BlockType.NAME
                    }

                    name == "p" && attributes["class"] == "V8SH_chapter" -> {
                        block = BlockType.NONE
                    }

                    name == "a" && block == BlockType.RELATED -> {
                        currentRelatedHref = attributes["href"] ?: ""
                    }

                    name == "a" && block == BlockType.TYPE -> {
                        if (typeNames.isNotEmpty()) {
                            typeNames += ", "
                        }
                    }
                }
            }
            .onText { text, handler ->
                val trimmed = text.trim()
                when (block) {
                    BlockType.NAME -> {
                        // Формат входной строки: "Справочники (Catalogs)"
                        val regex = Regex("([^(]+)\\s+\\(([^)]+)\\)")
                        val match = regex.find(trimmed)
                        if (match != null) {
                            propertyNameRu = match.groupValues[1].trim()
                            propertyNameEn = match.groupValues[2].trim()
                        }
                        block = BlockType.NONE
                    }

                    BlockType.NONE -> {
                        when (trimmed) {
                            "Использование:" -> block = BlockType.USAGE
                            "Описание:" -> {
                                block = BlockType.TYPE
                            }

                            "См. также:" -> block = BlockType.RELATED
                        }
                    }

                    BlockType.USAGE -> {
                        if (trimmed.contains("Только чтение.")) {
                            readonly = true
                        }
                    }

                    BlockType.TYPE -> {
                        when {
                            handler.currentTagName == "a" -> {
                                typeNames += trimmed
                            }

                            trimmed == "." -> {
                                block = BlockType.DESCRIPTION
                                handler.setMarkdownEnabled(true)
                            }
                        }
                    }

                    BlockType.DESCRIPTION -> {// empty
                    }

                    BlockType.RELATED -> {
                        if (currentRelatedHref.isNotEmpty() && trimmed.isNotEmpty() && trimmed != "," && trimmed != ".") {
                            relatedObjects.add(RelatedObject(name = trimmed, href = currentRelatedHref))
                            currentRelatedHref = ""
                        }
                    }
                }

            }
            .build()

        parsePage(inputStream, markdownHandler.createKsoupHandler())
        description.append(markdownHandler.getMarkdown().trimStart('.').trim())

        var finalDescription = description.toString().trim()
            .replace(" ,", ",")
            .replace(Regex("\\s{2,}"), " ")

        return PropertyInfo(
            propertyNameRu = propertyNameRu,
            propertyNameEn = propertyNameEn,
            description = finalDescription.trim(),
            readonly = readonly,
            typeName = typeNames,
            relatedObjects = relatedObjects
        )
    }
}

