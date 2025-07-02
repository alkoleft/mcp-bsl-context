/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.dto

import ru.alkoleft.context.core.ports.outgoing.FormatType

/**
 * DTO для запроса детальной информации об элементе API
 */
data class ElementInfoRequest(
    val elementId: String,
    val formatType: FormatType = FormatType.MARKDOWN,
    val includeMetadata: Boolean = true,
    val language: String = "ru",
) {
    init {
        require(elementId.isNotBlank()) { "Element ID cannot be blank" }
    }
}

/**
 * DTO для запроса информации о члене типа
 */
data class MemberInfoRequest(
    val typeName: String,
    val memberName: String,
    val formatType: FormatType = FormatType.MARKDOWN,
    val includeMetadata: Boolean = true,
    val language: String = "ru",
) {
    init {
        require(typeName.isNotBlank()) { "Type name cannot be blank" }
        require(memberName.isNotBlank()) { "Member name cannot be blank" }
    }
}

/**
 * DTO для запроса списка членов типа
 */
data class TypeMembersRequest(
    val typeName: String,
    val formatType: FormatType = FormatType.MARKDOWN,
    val includeConstructors: Boolean = false,
    val includeInherited: Boolean = false,
    val language: String = "ru",
) {
    init {
        require(typeName.isNotBlank()) { "Type name cannot be blank" }
    }
}
