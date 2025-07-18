/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search.indexes

import java.util.SortedMap

class StartWithIndex<T> : Index<T> {
    private lateinit var sortedValues: SortedMap<String, T>

    override fun load(
        items: List<T>,
        getter: (T) -> String,
    ) {
        sortedValues = items.associateBy { getter(it).lowercase() }.toSortedMap()
    }

    override fun get(key: String): List<T> {
        val searchKey = key.lowercase()
        val nextKey = searchKey + Char.MAX_VALUE
        return sortedValues
            .subMap(searchKey, nextKey)
            .map { it.value }
    }

    override val size: Int
        get() = sortedValues.size

    override fun isEmpty(): Boolean = sortedValues.isEmpty()
}
