/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.toc

/**
 * Модели данных для представления структуры HBK файла
 */
data class Chunk(
    val id: Int,
    val parentId: Int,
    val childCount: Int,
    val childIds: List<Int>,
    val properties: PropertiesContainer,
)

data class PropertiesContainer(
    val number1: Int,
    val number2: Int,
    val nameContainer: NameContainer,
    val htmlPath: String,
)

data class NameContainer(
    val number1: Int,
    val number2: Int,
    val nameObjects: List<NameObject>,
)

data class NameObject(
    val languageCode: String,
    val name: String,
)

typealias ChunkHandler = Chunk.() -> Unit
