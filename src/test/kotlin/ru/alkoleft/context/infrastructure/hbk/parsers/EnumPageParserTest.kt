/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.assertj.core.api.Assertions
import ru.alkoleft.context.infrastructure.hbk.parsers.core.PageParser
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.EnumPageParser
import java.io.FileInputStream
import java.nio.file.Paths
import kotlin.test.Test

class EnumPageParserTest {
    private fun <R> parseFile(
        fileName: String,
        parser: PageParser<R>,
    ): R {
        FileInputStream(Paths.get("src/test/resources/enums/$fileName").toFile()).use { inputStream ->
            return parser.parse(inputStream)
        }
    }

    @Test
    fun `test parse FormGroupType`() {
        val info = parseFile("FormGroupType.html", EnumPageParser())

        Assertions.assertThat(info.nameRu).isEqualTo("ВидГруппыФормы")
        Assertions.assertThat(info.nameEn).isEqualTo("FormGroupType")
        Assertions.assertThat(info.description).isEqualTo("Содержит варианты видов групп формы клиентского приложения.")
        Assertions.assertThat(info.relatedObjects).hasSize(2) // TODO
    }

    @Test
    fun `test parse PictureLib`() {
        val info = parseFile("PictureLib.html", EnumPageParser())

        Assertions.assertThat(info.nameRu).isEqualTo("БиблиотекаКартинок")
        Assertions.assertThat(info.nameEn).isEqualTo("PictureLib")
        Assertions
            .assertThat(info.description)
            .isEqualTo("Определяет набор картинок, используемых в конфигурации. Значения этого набора имеют тип `Картинка`.")
    }
}
