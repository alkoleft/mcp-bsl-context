/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.application.dto

/**
 * DTO для запроса загрузки контекста платформы
 */
data class LoadContextRequest(
    val path: String,
    val forceReload: Boolean = false,
    val validatePath: Boolean = true,
) {
    init {
        require(path.isNotBlank()) { "Context path cannot be blank" }
    }
}

/**
 * DTO для ответа о статусе контекста
 */
data class ContextStatusResponse(
    val isLoaded: Boolean,
    val path: String?,
    val lastUpdate: String?,
    val elementsCount: Int,
    val totalSources: Int,
    val errors: List<String> = emptyList(),
    val version: String?,
    val loadTimeMs: Long?,
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
    val isHealthy: Boolean get() = isLoaded && !hasErrors
}

/**
 * DTO для ответа о загрузке контекста
 */
data class LoadContextResponse(
    val success: Boolean,
    val path: String,
    val elementsLoaded: Int,
    val loadTimeMs: Long,
    val version: String?,
    val sources: List<String>,
    val errors: List<String> = emptyList(),
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()
}
