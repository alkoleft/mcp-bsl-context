/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

object Helper {
    fun readName(text: String): Pair<String, String>? {
        val regex = Regex("([^(]+)\\s*\\(([^)]+)\\)")
        return regex.find(text)?.let {
            return  Pair(it.groupValues[1].trim(), it.groupValues[2].trim())
        }
    }
}