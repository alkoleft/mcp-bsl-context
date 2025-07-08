/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.parsers

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.FileInputStream
import java.nio.file.Paths
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertyPageParserTest {
    private fun parseFile(
        fileName: String,
        directory: String = "global-properties",
    ): PropertyInfo {
        val parser = PropertyPageParser()
        FileInputStream(Paths.get("src/test/resources/$directory/$fileName").toFile()).use { inputStream ->
            return parser.parse(inputStream)
        }
    }

    @Test
    fun shouldCorrectlyParseCatalogs336Html() {
        val info = parseFile("Catalogs336.html")

        Assertions.assertThat(info.nameRu).isEqualTo("Справочники")
        Assertions.assertThat(info.nameEn).isEqualTo("Catalogs")
        Assertions.assertThat(info.readonly).isTrue()
        Assertions.assertThat(info.typeName).isEqualTo("СправочникиМенеджер")
        Assertions
            .assertThat(info.description)
            .isEqualTo("Используется для доступа к определенным в конфигурации справочникам.")
        Assertions.assertThat(info.relatedObjects).hasSize(1)
        Assertions.assertThat(info.relatedObjects?.get(0)?.name).isEqualTo("СправочникиМенеджер")
        Assertions.assertThat(info.relatedObjects?.get(0)?.href).contains("CatalogsManager.html")
        Assertions
            .assertThat(info.relatedObjects?.get(0)?.href)
            .isEqualTo("v8help://SyntaxHelperContext/objects/catalog125/catalog126/CatalogsManager.html")
    }

    @Test
    fun shouldCorrectlyParseURLExternalDataStorage12781Html() {
        val info = parseFile("URLExternalDataStorage12781.html")

        Assertions.assertThat(info.nameRu).isEqualTo("ХранилищеВнешнихДанныхНавигационныхСсылок")
        Assertions.assertThat(info.nameEn).isEqualTo("URLExternalDataStorage")
        Assertions.assertThat(info.readonly).isTrue()
        Assertions
            .assertThat(info.typeName)
            .isEqualTo("СтандартноеХранилищеНастроекМенеджер,ХранилищеНастроекМенеджер.<Имя хранилища>")
        Assertions
            .assertThat(info.description)
            .contains("Предоставляет доступ к хранилищу внешних данных навигационных ссылок.")
        Assertions.assertThat(info.relatedObjects).isNullOrEmpty()
    }

    @Test
    fun shouldCorrectlyParseWorkingDateUse1182Html() {
        val info = parseFile("WorkingDateUse1182.html")

        Assertions.assertThat(info.nameRu).isEqualTo("ИспользованиеРабочейДаты")
        Assertions.assertThat(info.nameEn).isEqualTo("WorkingDateUse")
        Assertions.assertThat(info.readonly).isTrue()
        Assertions.assertThat(info.typeName).isEqualTo("РежимРабочейДаты")
        Assertions.assertThat(info.description).contains("Определяет режим использования рабочей даты.")
        Assertions.assertThat(info.relatedObjects?.map { it.name }).contains("РабочаяДата")
    }

    @Test
    fun shouldCorrectlyParseXDTOFactory4693Html() {
        val info = parseFile("XDTOFactory4693.html")

        Assertions.assertThat(info.nameRu).isEqualTo("ФабрикаXDTO")
        Assertions.assertThat(info.nameEn).isEqualTo("XDTOFactory")
        Assertions.assertThat(info.readonly).isTrue()
        Assertions.assertThat(info.typeName).isEqualTo("ФабрикаXDTO")
        Assertions.assertThat(info.relatedObjects).isNullOrEmpty()
        assertEquals(
            """
            Фабрика XDTO, содержащая набор пакетов XDTO, соответствующих контексту выполнения:
            * для тонкого клиента, мобильного клиента и мобильного сервера - предопределенные пакеты (например, пакет типов XML-схемы)
            * для толстого клиента и сервера - все пакеты XDTO, имеющиеся в конфигурации, а также все предопределенные пакеты (например, пакет типов XML-схемы).
            """.trimIndent(),
            info.description,
        )
    }

    @Test
    fun shouldCorrectlyParseAttributes4786Html() {
        val info = parseFile("Attributes4786.html", "object-properties")

        Assertions.assertThat(info.nameRu).isEqualTo("Атрибуты")
        Assertions.assertThat(info.nameEn).isEqualTo("Attributes")
        Assertions.assertThat(info.readonly).isTrue()
        Assertions.assertThat(info.typeName).isEqualTo("КоллекцияАтрибутовDOM")
        assertEquals(
            """Содержит коллекцию атрибутов узла. Коллекция атрибутов доступна только для узла Element.
Узел Атрибуты
* Attribute - `Неопределено`;
* CDATASection - `Неопределено`;
* Comment - `Неопределено`;
* Document - `Неопределено`;
* DocumentFragment - `Неопределено`;
* DocumentType - `Неопределено`;
* Element - `КоллекцияАтрибутовDOM`;
* Entity - `Неопределено`;
* EntityReference - `Неопределено`;
* Notation - `Неопределено`;
* ProcessingInstruction - `Неопределено`;
* Text - `Неопределено`.""",
            info.description,
        )
        Assertions.assertThat(info.relatedObjects).isNullOrEmpty()
    }
}
