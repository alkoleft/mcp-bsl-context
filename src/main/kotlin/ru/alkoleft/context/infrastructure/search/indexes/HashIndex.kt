/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search.indexes

import java.util.concurrent.ConcurrentHashMap

class HashIndex<T> : Index<T> {
    private val index = ConcurrentHashMap<String, T>()

    override val size: Int
        get() = index.size

    override fun isEmpty() = index.isEmpty()

    override fun load(
        items: List<T>,
        getter: (T) -> String,
    ) {
        items.associateByTo(index) {
            getter(it).lowercase()
        }
    }

    override fun get(key: String): List<T> {
        val value = index[key.lowercase()]
        return if (value != null) listOf(value) else emptyList()
    }
}
