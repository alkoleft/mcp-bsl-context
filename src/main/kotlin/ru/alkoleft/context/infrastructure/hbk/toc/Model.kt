/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

/**
 * Модели данных для представления структуры HBK файла.
 *
 * Этот файл содержит классы данных, используемые для представления
 * иерархической структуры оглавления (Table of Contents) HBK файлов
 * платформы 1С:Предприятие.
 */

/**
 * Представляет чанк (фрагмент) в структуре оглавления HBK файла.
 *
 * Чанк является основным элементом иерархической структуры оглавления
 * и содержит информацию о странице документации, включая связи
 * с родительскими и дочерними элементами.
 *
 * @property id Уникальный идентификатор чанка
 * @property parentId Идентификатор родительского чанка
 * @property childCount Количество дочерних чанков
 * @property childIds Список идентификаторов дочерних чанков
 * @property properties Контейнер со свойствами чанка
 */
data class Chunk(
    val id: Int,
    val parentId: Int,
    val childCount: Int,
    val childIds: List<Int>,
    val properties: PropertiesContainer,
)

/**
 * Контейнер свойств чанка оглавления.
 *
 * Содержит метаданные чанка, включая числовые параметры,
 * контейнер с названиями и путь к HTML файлу.
 *
 * @property number1 Первый числовой параметр (служебное поле)
 * @property number2 Второй числовой параметр (служебное поле)
 * @property nameContainer Контейнер с названиями на разных языках
 * @property htmlPath Путь к HTML файлу страницы документации
 */
data class PropertiesContainer(
    val number1: Int,
    val number2: Int,
    val nameContainer: NameContainer,
    val htmlPath: String,
)

/**
 * Контейнер названий чанка на разных языках.
 *
 * Содержит числовые параметры и список объектов названий,
 * каждый из которых представляет название на определенном языке.
 *
 * @property number1 Первый числовой параметр (служебное поле)
 * @property number2 Второй числовой параметр (служебное поле)
 * @property nameObjects Список объектов названий на разных языках
 */
data class NameContainer(
    val number1: Int,
    val number2: Int,
    val nameObjects: List<NameObject>,
)

/**
 * Объект названия на определенном языке.
 *
 * Представляет название элемента документации на конкретном языке
 * с указанием языкового кода.
 *
 * @property languageCode Код языка (например, "ru", "en")
 * @property name Название элемента на указанном языке
 */
data class NameObject(
    val languageCode: String,
    val name: String,
)
