/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import ru.alkoleft.context.exceptions.PlatformContextLoadException
import ru.alkoleft.context.infrastructure.hbk.HbkContentReader
import ru.alkoleft.context.infrastructure.hbk.Page
import ru.alkoleft.context.infrastructure.hbk.pages.PageParser

class PlatformContextPagesParser(
    private val context: HbkContentReader.Context,
) {
    val propertyPageParser = PropertyPageParser()
    val methodPageParser = MethodPageParser()
    val enumPageParser = EnumPageParser()
    val enumValuePageParser = EnumValuePageParser()
    val objectPageParser = ObjectPageParser()
    val constructorPageParser = ConstructorPageParser()

    fun parsePropertyPage(page: Page) = parsePage(page, propertyPageParser)

    fun parseMethodPage(page: Page) = parsePage(page, methodPageParser)

    fun parseEnumPage(page: Page): EnumInfo = parsePage(page, enumPageParser)

    fun parseEnumValuePage(page: Page) = parsePage(page, enumValuePageParser)

    fun parseObjectPage(page: Page): ObjectInfo = parsePage(page, objectPageParser)

    fun parseConstructorPage(page: Page) = parsePage(page, constructorPageParser)

    private fun <T> parsePage(
        page: Page,
        parser: PageParser<T>,
    ): T {
        try {
            return context.getEntryStream(page).use { parser.parse(it) }
        } catch (ex: Exception) {
            throw PlatformContextLoadException("Не удалось разобрать страницу документации $page", ex)
        }
    }
}
