/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.infrastructure.hbk.HbkContentReader
import ru.alkoleft.context.infrastructure.hbk.Page

class PlatformContextPagesParser(private val context: HbkContentReader.Context) {
    val parser: PropertyPageParser = PropertyPageParser()
    fun parsePropertyPage(page: Page) =
        context.getEntryStream(page)?.use { parser.parse(it) }
}