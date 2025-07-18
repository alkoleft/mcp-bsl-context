/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.models

/**
 * Представляет страницу документации в HBK файле.
 *
 * Каждая страница содержит заголовок на двух языках, путь к HTML файлу
 * и список дочерних страниц, формируя иерархическую структуру документации.
 *
 * @property title Заголовок страницы на двух языках
 * @property htmlPath Путь к HTML файлу в архиве
 * @property children Список дочерних страниц
 */
data class Page(
    val title: DoubleLanguageString,
    val htmlPath: String,
    val children: MutableList<Page> = mutableListOf(),
)

/**
 * Строка на двух языках (русский и английский).
 *
 * Используется для представления названий и заголовков в документации
 * платформы 1С:Предприятие, которая поддерживает двуязычность.
 *
 * @property en Английская версия строки
 * @property ru Русская версия строки
 */
data class DoubleLanguageString(
    val en: String,
    val ru: String,
)
