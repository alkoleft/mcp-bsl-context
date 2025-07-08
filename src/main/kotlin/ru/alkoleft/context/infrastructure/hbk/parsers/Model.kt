/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

data class PropertyInfo(
    val propertyNameRu: String,
    val propertyNameEn: String,
    val description: String,
    val readonly: Boolean,
    val typeName: String,
    val note: String?,
    val relatedObjects: List<RelatedObject>?
)

data class MethodInfo(
    val nameRu: String,
    val nameEn: String,
    val signatures: List<MethodSignatureInfo>,
    val example: String?,
    val note: String?,
    val relatedObjects: List<RelatedObject>?,
)
data class MethodParameterInfo(
    val name: String,
    val type: String,
    val isOptional: Boolean,
    val description: String,
)

data class EnumInfo(
    val nameRu: String,
    val nameEn: String,
    val description: String,
    val relatedObjects: List<RelatedObject>?,
    val values: MutableList<EnumValueInfo> = mutableListOf(),
    var example: String?
)

data class EnumValueInfo(
    val nameRu: String,
    val nameEn: String,
    val description: String,
    val relatedObjects: List<RelatedObject>?,
)

/**
 * Информация о глобальном свойстве
 */
data class RelatedObject(
    val name: String,
    val href: String
)

data class ValueInfo(
    val type: String,
    val description: String,
)

data class MethodSignatureInfo(
    var name: String,
    var syntax: String,
    var parameters: List<MethodParameterInfo>,
    var returnValue: ValueInfo?,
    var description: String,
)
