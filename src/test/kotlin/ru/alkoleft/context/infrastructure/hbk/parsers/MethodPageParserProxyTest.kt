/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.junit.jupiter.api.Test
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.MethodPageParser
import java.io.File
import kotlin.test.assertEquals

class MethodPageParserProxyTest {
    @Test
    fun `test parsing method with proxy handlers`() {
        val file = File("src/test/resources/global-methods/GetCommonTemplate376.html")
        val parser = MethodPageParser()
        val result = parser.parse(file.inputStream())

        assertEquals("GetCommonTemplate", result.nameEn)
        assertEquals("ПолучитьОбщийМакет", result.nameRu)
        assertEquals(1, result.signatures.size)
        assertEquals("Основная", result.signatures[0].name)
        assertEquals(1, result.signatures[0].parameters.size)
        assertEquals("ОбщийМакет", result.signatures[0].parameters[0].name)
        assertEquals(false, result.signatures[0].parameters[0].isOptional)
        assertEquals("Строка,ОбъектМетаданных: Макет", result.signatures[0].parameters[0].type)
        assertEquals(
            "Имя общего макета, как оно задано в конфигураторе, или объект описания метаданного общего макета.",
            result.signatures[0].parameters[0].description,
        )
        assertEquals(
            "ТабличныйДокумент,ТекстовыйДокумент; другой объект, который может быть макетом.",
            result.signatures[0].returnValue?.type,
        )
        assertEquals("", result.signatures[0].returnValue?.description)
        assertEquals("Получает один из общих макетов конфигурации.", result.signatures[0].description)
        assertEquals(
            """
            // Получение общего макета по имени

            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет("СтруктураКонфигурации");
            // Получение общего макета по объекту описания метаданного
            
            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет(Метаданные.ОбщиеМакеты.СтруктураКонфигурации);
            """.trimIndent(),
            result.example,
        )
    }
}
