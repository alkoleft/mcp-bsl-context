/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.pages

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ru.alkoleft.context.infrastructure.hbk.parsers.PropertyInfo
import ru.alkoleft.context.infrastructure.hbk.parsers.PropertyPageParser
import java.io.FileInputStream
import java.nio.file.Paths

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PropertyPageParserTest {

    private fun parseFile(fileName: String, directory: String = "global-properties"): PropertyInfo {
        val parser = PropertyPageParser()
        FileInputStream(Paths.get("src/test/resources/$directory/$fileName").toFile()).use { inputStream ->
            return parser.parse(inputStream)
        }
    }

    @Test
    fun shouldCorrectlyParseCatalogs336Html() {
        val info = parseFile("Catalogs336.html")
        
        assertThat(info.propertyNameRu).isEqualTo("Справочники")
        assertThat(info.propertyNameEn).isEqualTo("Catalogs")
        assertThat(info.readonly).isTrue()
        assertThat(info.typeName).isEqualTo("СправочникиМенеджер")
        assertThat(info.description).isEqualTo("Используется для доступа к определенным в конфигурации справочникам.")
        assertThat(info.relatedObjects).hasSize(1)
        assertThat(info.relatedObjects[0].name).isEqualTo("СправочникиМенеджер")
        assertThat(info.relatedObjects[0].href).contains("CatalogsManager.html")
        assertThat(info.relatedObjects[0].href).isEqualTo("v8help://SyntaxHelperContext/objects/catalog125/catalog126/CatalogsManager.html")
    }

    @Test
    fun shouldCorrectlyParseURLExternalDataStorage12781Html() {
        val info = parseFile("URLExternalDataStorage12781.html")
        
        assertThat(info.propertyNameRu).isEqualTo("ХранилищеВнешнихДанныхНавигационныхСсылок")
        assertThat(info.propertyNameEn).isEqualTo("URLExternalDataStorage")
        assertThat(info.readonly).isTrue()
        assertThat(info.typeName).isEqualTo("СтандартноеХранилищеНастроекМенеджер, ХранилищеНастроекМенеджер.<Имя хранилища>")
        assertThat(info.description).contains("Предоставляет доступ к хранилищу внешних данных навигационных ссылок.")
        assertThat(info.relatedObjects).hasSize(0)
    }

    @Test
    fun shouldCorrectlyParseWorkingDateUse1182Html() {
        val info = parseFile("WorkingDateUse1182.html")
        
        assertThat(info.propertyNameRu).isEqualTo("ИспользованиеРабочейДаты")
        assertThat(info.propertyNameEn).isEqualTo("WorkingDateUse")
        assertThat(info.readonly).isTrue()
        assertThat(info.typeName).isEqualTo("РежимРабочейДаты")
        assertThat(info.description).contains("Определяет режим использования рабочей даты.")
        assertThat(info.relatedObjects.map { it.name }).contains("РабочаяДата")
    }

    @Test
    fun shouldCorrectlyParseXDTOFactory4693Html() {
        val info = parseFile("XDTOFactory4693.html")
        
        assertThat(info.propertyNameRu).isEqualTo("ФабрикаXDTO")
        assertThat(info.propertyNameEn).isEqualTo("XDTOFactory")
        assertThat(info.readonly).isTrue()
        assertThat(info.typeName).isEqualTo("ФабрикаXDTO")
        assertThat(info.description).contains("Фабрика XDTO, содержащая набор пакетов XDTO")
        assertThat(info.relatedObjects).hasSize(0)
    }

    @Test
    fun shouldCorrectlyParseAttributes4786Html() {
        val info = parseFile("Attributes4786.html", "object-properties")
        
        assertThat(info.propertyNameRu).isEqualTo("Атрибуты")
        assertThat(info.propertyNameEn).isEqualTo("Attributes")
        assertThat(info.readonly).isTrue()
        assertThat(info.typeName).isEqualTo("КоллекцияАтрибутовDOM")
        assertThat(info.description).isEqualTo("""Содержит коллекцию атрибутов узла. Коллекция атрибутов доступна только для узла Element.
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
* Text - `Неопределено`.""")
        assertThat(info.relatedObjects).hasSize(0)
    }
}