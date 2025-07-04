/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.persistent.repositories

import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.persistent.PlatformContextRepository
import ru.alkoleft.context.business.valueobjects.ApiType
import ru.alkoleft.context.business.valueobjects.SearchQuery
import ru.alkoleft.context.infrastructure.persistent.storage.PlatformContextStorage
import ru.alkoleft.context.infrastructure.search.SearchEngine

class PlatformRepository(
    val searchEngine: SearchEngine,
    val context: PlatformContextStorage,
) : PlatformContextRepository {
    override fun search(
        query: String,
        limit: Int,
        type: ApiType?,
    ): List<Definition> =
        search(
            SearchQuery(
                query = query,
                apiType = type,
                maxResults = limit,
            ),
        )

    override fun search(searchQuery: SearchQuery) = searchEngine.search(searchQuery)

    override fun findType(name: String) = searchEngine.findType(name)

    override fun findProperty(name: String) = searchEngine.findProperty(name)

    override fun findMethod(name: String) = searchEngine.findMethod(name)

    override fun findTypeMember(
        type: PlatformTypeDefinition,
        memberName: String,
    ) = searchEngine.findTypeMember(type, memberName)
}
