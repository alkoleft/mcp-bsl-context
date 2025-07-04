/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search.indexes

import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition

class Indexes(
    val properties: Index<PropertyDefinition>,
    val methods: Index<MethodDefinition>,
    val types: Index<PlatformTypeDefinition>,
) {
    fun getProperties(key: String) = properties.get(key)

    fun getMethods(key: String) = methods.get(key)

    fun getTypes(key: String) = types.get(key)
}
