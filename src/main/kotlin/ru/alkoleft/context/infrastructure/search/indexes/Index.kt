/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.search.indexes

interface Index<T> {
    fun load(
        items: List<T>,
        getter: (T) -> String,
    )

    fun get(key: String): List<T>

    val size: Int

    fun isEmpty(): Boolean
}
