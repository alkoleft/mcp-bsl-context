/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

private val NAMES_PATTERN = """([^(]+)\s*\(([^)]+)\)""".toRegex()

object Helper {
    fun readName(text: CharSequence): Pair<String, String> {
        if (text.isBlank()) {
            throw IllegalArgumentException("Имя страницы должно быть заполнено")
        }
        val match = NAMES_PATTERN.find(text)

        return if (match != null) {
            Pair(match.groupValues[1].trim(), match.groupValues[2].trim())
        } else {
            Pair(text.toString(), "")
        }
    }
}
