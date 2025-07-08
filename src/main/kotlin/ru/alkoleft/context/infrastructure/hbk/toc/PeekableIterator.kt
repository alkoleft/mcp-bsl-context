/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

/**
 * Расширение для Iterator для поддержки peek операции
 */
class PeekableIterator<T>(
    private val iterator: Iterator<T>,
) : Iterator<T> {
    private var peeked: T? = null

    override fun hasNext(): Boolean = peeked != null || iterator.hasNext()

    override fun next(): T =
        if (peeked != null) {
            val result = peeked!!
            peeked = null
            result
        } else {
            iterator.next()
        }

    fun peek(): T? {
        if (peeked == null && iterator.hasNext()) {
            peeked = iterator.next()
        }
        return peeked
    }
}
