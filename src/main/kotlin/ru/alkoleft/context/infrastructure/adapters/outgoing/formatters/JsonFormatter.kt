/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.formatters

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import ru.alkoleft.context.core.domain.api.ApiElement
import ru.alkoleft.context.core.domain.search.SearchResult
import ru.alkoleft.context.core.ports.outgoing.FormatType
import ru.alkoleft.context.core.ports.outgoing.ResultFormatter

/**
 * JSON форматтер для результатов поиска
 * Предоставляет структурированные данные в JSON формате
 */
@Component
class JsonFormatter : ResultFormatter {
    override val formatType = FormatType.JSON

    override fun getMimeType(): String = "application/json"

    private val objectMapper: ObjectMapper =
        jacksonObjectMapper().apply {
            configure(SerializationFeature.INDENT_OUTPUT, true)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

    /**
     * Форматирование результатов поиска в JSON
     */
    override suspend fun formatSearchResults(result: SearchResult): String {
        val jsonResult =
            JsonSearchResult(
                query =
                    JsonQuery(
                        text = result.query.text,
                        algorithm = result.algorithm.name,
                        options =
                            JsonSearchOptions(
                                elementTypes =
                                    result.query.options.elementTypes
                                        .map { it.name },
                                dataSources =
                                    result.query.options.dataSources
                                        .map { it.name },
                                limit = result.query.options.limit,
                                exactMatch = result.query.options.exactMatch,
                                caseSensitive = result.query.options.caseSensitive,
                            ),
                    ),
                statistics =
                    JsonStatistics(
                        totalCount = result.totalCount,
                        foundCount = result.items.size,
                        executionTimeMs = result.executionTimeMs,
                        algorithm = result.algorithm.name,
                        limitReached = result.limitReached,
                    ),
                items =
                    result.items.map { item ->
                        JsonResultItem(
                            element = mapElementToJson(item.element),
                            relevanceScore = item.relevanceScore,
                            matchReason = formatMatchReason(item.matchReason),
                            highlightedText = item.highlightedText,
                        )
                    },
            )

        return objectMapper.writeValueAsString(jsonResult)
    }

    /**
     * Форматирование детальной информации об элементе в JSON
     */
    override suspend fun formatDetailedInfo(element: ApiElement): String {
        val jsonElement = mapElementToJson(element)
        return objectMapper.writeValueAsString(jsonElement)
    }

    /**
     * Форматирование списка элементов в JSON
     */
    override suspend fun formatElementList(
        elements: List<ApiElement>,
        title: String?,
    ): String {
        val jsonResult =
            JsonElementList(
                title = title,
                totalCount = elements.size,
                elements = elements.map { mapElementToJson(it) },
            )
        return objectMapper.writeValueAsString(jsonResult)
    }

    /**
     * Преобразование доменного элемента в JSON структуру
     */
    private fun mapElementToJson(element: ApiElement): JsonElement =
        when (element) {
            is ru.alkoleft.context.core.domain.api.Method ->
                JsonElement(
                    id = element.id,
                    name = element.name,
                    type = "METHOD",
                    description = element.description,
                    source = element.source.name,
                    isGlobal = element.isGlobal,
                    parentType = element.parentType?.name,
                    returnType = element.returnType?.name,
                    signatures =
                        element.signatures.map { signature ->
                            JsonSignature(
                                parameters =
                                    signature.parameters.map { param ->
                                        JsonParameter(
                                            name = param.name,
                                            type = param.type.name,
                                            isOptional = param.isOptional,
                                            defaultValue = param.defaultValue,
                                            description = param.description,
                                        )
                                    },
                                returnType = signature.returnType?.name,
                                isDeprecated = signature.isDeprecated,
                            )
                        },
                )

            is ru.alkoleft.context.core.domain.api.Property ->
                JsonElement(
                    id = element.id,
                    name = element.name,
                    type = "PROPERTY",
                    description = element.description,
                    source = element.source.name,
                    parentType = element.parentType?.name,
                    dataType = element.dataType.name,
                    isReadonly = element.isReadonly,
                )

            is ru.alkoleft.context.core.domain.api.Type ->
                JsonElement(
                    id = element.id,
                    name = element.name,
                    type = "TYPE",
                    description = element.description,
                    source = element.source.name,
                    methodsCount = element.methods.size,
                    propertiesCount = element.properties.size,
                    constructorsCount = element.constructors.size,
                )

            is ru.alkoleft.context.core.domain.api.Constructor ->
                JsonElement(
                    id = element.id,
                    name = element.name,
                    type = "CONSTRUCTOR",
                    description = element.description,
                    source = element.source.name,
                    parentType = element.parentType.name,
                    parameters =
                        element.parameters.map { param ->
                            JsonParameter(
                                name = param.name,
                                type = param.type.name,
                                isOptional = param.isOptional,
                                defaultValue = param.defaultValue,
                                description = param.description,
                            )
                        },
                )
        }

    /**
     * Форматирование причины совпадения для JSON
     */
    private fun formatMatchReason(matchReason: ru.alkoleft.context.core.domain.search.MatchReason): JsonMatchReason =
        when (matchReason) {
            is ru.alkoleft.context.core.domain.search.MatchReason.ExactMatch ->
                JsonMatchReason("EXACT_MATCH", "Точное совпадение", mapOf("field" to matchReason.field))

            is ru.alkoleft.context.core.domain.search.MatchReason.PrefixMatch ->
                JsonMatchReason("PREFIX_MATCH", "Совпадение префикса", mapOf("field" to matchReason.field))

            is ru.alkoleft.context.core.domain.search.MatchReason.ContainsMatch ->
                JsonMatchReason("CONTAINS_MATCH", "Содержит текст", mapOf("field" to matchReason.field))

            is ru.alkoleft.context.core.domain.search.MatchReason.FuzzyMatch ->
                JsonMatchReason(
                    "FUZZY_MATCH",
                    "Нечеткое совпадение",
                    mapOf("field" to matchReason.field, "similarity" to matchReason.similarity),
                )

            is ru.alkoleft.context.core.domain.search.MatchReason.SemanticMatch ->
                JsonMatchReason("SEMANTIC_MATCH", "Семантическое совпадение", mapOf("similarity" to matchReason.similarity))

            is ru.alkoleft.context.core.domain.search.MatchReason.MultipleMatches ->
                JsonMatchReason(
                    "MULTIPLE_MATCHES",
                    "Множественные совпадения",
                    mapOf("count" to matchReason.reasons.size),
                )
        }
}

// JSON DTO классы для сериализации

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonSearchResult(
    val query: JsonQuery,
    val statistics: JsonStatistics,
    val items: List<JsonResultItem>,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonQuery(
    val text: String,
    val algorithm: String,
    val options: JsonSearchOptions,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonSearchOptions(
    val elementTypes: List<String>,
    val dataSources: List<String>,
    val limit: Int,
    val exactMatch: Boolean,
    val caseSensitive: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonStatistics(
    val totalCount: Int,
    val foundCount: Int,
    val executionTimeMs: Long,
    val algorithm: String,
    val limitReached: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonResultItem(
    val element: JsonElement,
    val relevanceScore: Double,
    val matchReason: JsonMatchReason,
    val highlightedText: String?,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonElement(
    val id: String,
    val name: String,
    val type: String,
    val description: String,
    val source: String,
    val isGlobal: Boolean? = null,
    val parentType: String? = null,
    val returnType: String? = null,
    val dataType: String? = null,
    val isReadonly: Boolean? = null,
    val methodsCount: Int? = null,
    val propertiesCount: Int? = null,
    val constructorsCount: Int? = null,
    val signatures: List<JsonSignature>? = null,
    val parameters: List<JsonParameter>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonSignature(
    val parameters: List<JsonParameter>,
    val returnType: String?,
    val isDeprecated: Boolean,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonParameter(
    val name: String,
    val type: String,
    val isOptional: Boolean,
    val defaultValue: String?,
    val description: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonMatchReason(
    val type: String,
    val description: String,
    val metadata: Map<String, Any>? = null,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class JsonElementList(
    val title: String?,
    val totalCount: Int,
    val elements: List<JsonElement>,
)
