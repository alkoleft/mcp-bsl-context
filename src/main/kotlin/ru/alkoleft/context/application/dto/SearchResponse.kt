/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.dto

/**
 * DTO для ответа на поисковый запрос на уровне приложения
 * Содержит отформатированные результаты и метаданные
 */
data class SearchResponse(
    // Исходный запрос
    val query: String,
    // Отформатированный результат
    val result: String,
    // Метаданные поиска
    val totalCount: Int,
    val executionTimeMs: Long,
    val algorithm: String,
    val hasMore: Boolean,
    // Дополнительная информация
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SUCCESS",
) {
    /**
     * Проверка, есть ли результаты
     */
    val hasResults: Boolean get() = totalCount > 0

    /**
     * Производительность поиска
     */
    val isSlowSearch: Boolean get() = executionTimeMs > 1000
}
