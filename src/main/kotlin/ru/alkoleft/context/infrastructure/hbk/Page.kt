/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

data class Page(
    val title: DoubleLanguageString,
    val htmlPath: String,
    val children: MutableList<Page> = mutableListOf()
)

data class DoubleLanguageString(
    val en: String,
    val ru: String
)