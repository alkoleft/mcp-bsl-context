/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.EnumValuePageParser
import java.io.File

class EnumValuePageParserTest {
    @Test
    fun `test parse ControlBorder1060 - enum value with description`() {
        val parser = EnumValuePageParser()
        val file = File("src/test/resources/enum-values/ControlBorder1060.html")
        val result = parser.parse(file.inputStream())

        assertEquals("РамкаЭлементаУправления", result.nameRu)
        assertEquals("ControlBorder", result.nameEn)
        assertEquals(
            """
            Тип: `Рамка`. 
            Рамка элементов управления.
            """.trimIndent(),
            result.description,
        )

        // Проверяем связанные объекты
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse UsualGroup7012 - enum value with detailed description`() {
        val parser = EnumValuePageParser()
        val file = File("src/test/resources/enum-values/UsualGroup7012.html")
        val result = parser.parse(file.inputStream())

        assertEquals("ОбычнаяГруппа", result.nameRu)
        assertEquals("UsualGroup", result.nameEn)
        assertEquals(
            "Обычная группа. Группа данного вида может содержать поля, таблицы, кнопки и группы видов `ОбычнаяГруппа`, `КоманднаяПанель`, `Страницы`.",
            result.description,
        )

        // Проверяем связанные объекты
        assertNull(result.relatedObjects)
    }
}
