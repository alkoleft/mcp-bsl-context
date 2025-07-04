/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.persistent

import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition
import ru.alkoleft.context.business.valueobjects.ApiType
import ru.alkoleft.context.business.valueobjects.SearchQuery

interface PlatformContextRepository {
    fun search(
        query: String,
        limit: Int,
        type: ApiType? = null,
    ): List<Definition>

    fun search(searchQuery: SearchQuery): List<Definition>

    fun findType(name: String): PlatformTypeDefinition?

    fun findProperty(name: String): PropertyDefinition?

    fun findMethod(name: String): MethodDefinition?

    fun findTypeMember(
        type: PlatformTypeDefinition,
        memberName: String,
    ): Definition?
}
