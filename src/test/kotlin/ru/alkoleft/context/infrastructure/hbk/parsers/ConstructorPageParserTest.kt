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
import ru.alkoleft.context.infrastructure.hbk.parsers.specialized.ConstructorPageParser
import java.io.File

class ConstructorPageParserTest {
    @Test
    fun `test parse ctor13 - Array constructor with parameters`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor13.html")
        val result = parser.parse(file.inputStream())

        assertEquals("По количеству элементов", result.name)
        assertEquals("Новый Массив(<КоличествоЭлементов1>,...,<КоличествоЭлементовN>)", result.syntax)

        // Проверяем параметры
        assertEquals(1, result.parameters.size)
        val parameter = result.parameters[0]
        assertEquals("КоличествоЭлементов1>,...,<КоличествоЭлементовN", parameter.name)
        assertEquals("Число", parameter.type)
        assertTrue(parameter.isOptional)
        assertEquals(
            "Каждый параметр определяет количество элементов массива в соответствующем измерении. Может задаваться неограниченное количество параметров. Если ни один параметр не указан, то создается одномерный массив с нулевым количеством элементов.",
            parameter.description,
        )

        // Проверяем описание
        assertEquals(
            "Создает массив из указанного количества элементов. Если задано несколько параметров, то будет создан массив, элементами которого являются массивы (и т.д. в зависимости от количества параметров). Фактически конструктор позволяет создать массивы массивов, которые могут являться аналогом многомерного массива.",
            result.description,
        )

        // Проверяем пример
        assertNotNull(result.example)
        assertEquals(
            """
                // массив с 0 элементами
                
                Массив1 = Новый Массив;
                
                // массив из 10 элементов, 
                
                // каждый из которых является массивом из 2 элементов,
                
                // каждый из которых является массивом из 4 элементов
                
                Массив2 = Новый Массив(10,2,4);
            """.trimIndent(),
            result.example,
        )

        assertNull(result.note)
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse ctor80 - ZipFileWriter constructor without parameters`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor80.html")
        val result = parser.parse(file.inputStream())

        assertEquals("Формирование неинициализированного объекта", result.name)
        assertEquals("Новый ЗаписьZipФайла()", result.syntax)

        // Проверяем параметры - их не должно быть
        assertEquals(0, result.parameters.size)

        // Проверяем описание
        assertEquals("Создает неинициализированный объект `ЗаписьZipФайла`.", result.description)

        assertNull(result.example)
        assertNull(result.note)
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse ctor225 - Constructor with single required parameter`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor225.html")
        val result = parser.parse(file.inputStream())

        assertEquals("На основании имени панели", result.name)
        assertEquals("Новый ЭлементНастройкиСоставаИнтерфейсаКлиентскогоПриложения(<Имя>)", result.syntax)

        // Проверяем параметры
        assertEquals(1, result.parameters.size)
        val parameter = result.parameters[0]
        assertEquals("Имя", parameter.name)
        assertEquals("Строка", parameter.type)
        assertTrue(!parameter.isOptional) // обязательный параметр
        assertEquals("Имя панели.", parameter.description)

        // Проверяем описание
        assertEquals("Создает элемент настройки на основании имени панели.", result.description)

        assertNull(result.example)
        assertNull(result.note)
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse ctor81 - Constructor with multiple parameters`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor81.html")
        val result = parser.parse(file.inputStream())

        assertEquals("На основании имени файла", result.name)
        assertEquals(
            "Новый ЗаписьZipФайла(<ИмяФайла>, <Пароль>, <Комментарий>, <МетодСжатия>, <УровеньСжатия>, <МетодШифрования>, <Кодировка>)",
            result.syntax,
        )

        // Проверяем параметры
        assertEquals(7, result.parameters.size)

        // Проверяем первый параметр (обязательный)
        val firstParam = result.parameters[0]
        assertEquals("ИмяФайла", firstParam.name)
        assertEquals("Строка", firstParam.type)
        assertTrue(!firstParam.isOptional)
        assertEquals("Имя файла, куда будет записан архив.", firstParam.description)

        // Проверяем второй параметр (необязательный)
        val secondParam = result.parameters[1]
        assertEquals("Пароль", secondParam.name)
        assertEquals("Строка", secondParam.type)
        assertTrue(secondParam.isOptional)
        assertEquals(
            """
            Пароль, который будет назначен архиву. 
            Если пароль не назначен или является пустой строкой, то шифрование не происходит.
            """.trimIndent(),
            secondParam.description,
        )

        // Проверяем описание
        assertEquals("Создает ZIP файл для записи. Аналогичен методу `Открыть`.", result.description)

        assertNull(result.example)
        assertNull(result.note)
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse ctor264 - Constructor with stream parameter`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor264.html")
        val result = parser.parse(file.inputStream())

        assertEquals("На основании потока", result.name)
        assertEquals(
            "Новый ЗаписьZipФайла(<Поток>, <Пароль>, <Комментарий>, <МетодСжатия>, <УровеньСжатия>, <МетодШифрования>)",
            result.syntax,
        )

        // Проверяем параметры
        assertEquals(6, result.parameters.size)

        // Проверяем первый параметр (обязательный)
        val firstParam = result.parameters[0]
        assertEquals("Поток", firstParam.name)
        assertEquals("Поток,ПотокВПамяти,ФайловыйПоток", firstParam.type)
        assertTrue(!firstParam.isOptional)
        assertEquals("Поток, в который будет записан архив.", firstParam.description)

        // Проверяем описание
        assertEquals("Создает объект записи ZIP-архива и устанавливает поток для записи архива.", result.description)

        assertNull(result.example)
        assertNull(result.note)
        assertNull(result.relatedObjects)
    }

    @Test
    fun `test parse ctor_Auto - UserWorkFavorites default constructor`() {
        val parser = ConstructorPageParser()
        val file = File("src/test/resources/constructors/ctor_Auto.html")
        val result = parser.parse(file.inputStream())

        assertEquals("По умолчанию", result.name)
        assertEquals("Новый ИзбранноеРаботыПользователя", result.syntax)

        // Проверяем параметры - их не должно быть
        assertEquals(0, result.parameters.size)

        // Проверяем описание - в данном случае его нет
        assertEquals(result.description, "")

        assertNull(result.example)
        assertNull(result.note)
        assertNull(result.relatedObjects)
    }
}
