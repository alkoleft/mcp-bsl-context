/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.File

class MethodPageParserTest {
    @Test
    fun `test parse BeginGetFileFromServer5707`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/BeginGetFileFromServer5707.html")
        val result = parser.parse(file.inputStream())

        println("BeginGetFileFromServer signatures: ${result.signatures.size}")
        result.signatures.forEachIndexed { index, signature ->
            println("Signature $index: ${signature.name}")
            println("  Parameters: ${signature.parameters.size}")
            signature.parameters.forEach { param ->
                println("    - ${param.name}: ${param.type} (${if (param.isOptional) "optional" else "required"})")
            }
        }

        assertEquals("НачатьПолучениеФайлаССервера", result.nameRu)
        assertEquals("BeginGetFileFromServer", result.nameEn)
        assertEquals(2, result.signatures.size)

        // Проверяем, что у нас есть две сигнатуры с правильными именами
        assertTrue(result.signatures.any { it.name == "С диалогом" })
        assertTrue(result.signatures.any { it.name == "Без диалога" })

        // Находим сигнатуры по имени
        val signature1 = result.signatures.first { it.name == "С диалогом" }
        val signature2 = result.signatures.first { it.name == "Без диалога" }

        // Проверяем синтаксис
        assertTrue(signature1.syntax.contains("<Адрес>"))
        assertTrue(signature1.syntax.contains("<ИмяФайла>"))
        assertTrue(signature1.syntax.contains("<ПараметрыДиалогаПолученияФайлов>"))

        assertTrue(signature2.syntax.contains("<ОписаниеОповещенияОЗавершении>"))
        assertTrue(signature2.syntax.contains("<Адрес>"))
        assertTrue(signature2.syntax.contains("<ПутьКФайлу>"))

        // Проверяем наличие описания
        assertNotNull(signature1.description)
        assertNotNull(signature2.description)

        assertNull(result.example)
    }

    @Test
    fun `test parse GetCommonTemplate376`() {
        val parser = MethodPageParser()
        val file = File("src/test/resources/global-methods/GetCommonTemplate376.html")
        val result = parser.parse(file.inputStream())

        println("GetCommonTemplate signatures: ${result.signatures.size}")
        result.signatures.forEachIndexed { index, signature ->
            println("Signature $index: ${signature.name}")
            println("  Parameters: ${signature.parameters.size}")
            signature.parameters.forEach { param ->
                println("    - ${param.name}: ${param.type} (${if (param.isOptional) "optional" else "required"})")
            }
            println("  Return value: ${signature.returnValue?.type}")
        }

        assertEquals("ПолучитьОбщийМакет", result.nameRu)
        assertEquals("GetCommonTemplate", result.nameEn)

        // Проверяем, что описание метода было добавлено
        assertNotNull(result.example)
        assertEquals(
            """
            // Получение общего макета по имени

            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет("СтруктураКонфигурации");
            // Получение общего макета по объекту описания метаданного

            МакетСтруктурыКонфигураци = ПолучитьОбщийМакет(Метаданные.ОбщиеМакеты.СтруктураКонфигурации);
            """.trimIndent(),
            result.example,
        )

        // Проверяем, что есть хотя бы одна сигнатура
        assertTrue(result.signatures.size >= 1)

        // Если есть сигнатуры, проверяем первую
        if (result.signatures.isNotEmpty()) {
            val signature = result.signatures[0]

            // Проверка возвращаемого значения
            assertNotNull(signature.returnValue)
            val returnValue = signature.returnValue!!
            println("Return value type: ${returnValue.type}")
            println("Return value description: ${returnValue.description}")

            // Проверяем, что тип возвращаемого значения содержит нужные части
            val returnValueType = returnValue.type.lowercase()
            assertTrue(returnValueType.contains("табличныйдокумент") || returnValueType.contains("текстовыйдокумент"))
        }
    }
}
