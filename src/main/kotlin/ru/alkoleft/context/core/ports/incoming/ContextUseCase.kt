/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.core.ports.incoming

import ru.alkoleft.context.core.domain.api.DataSource

/**
 * Контекст платформы с метаинформацией
 */
data class PlatformContext(
    val path: String,
    val loadedAt: java.time.Instant,
    val totalElements: Int,
    val sources: Set<DataSource>,
    val version: String? = null,
)

/**
 * Статус контекста платформы
 */
data class ContextStatus(
    val isLoaded: Boolean,
    val lastUpdate: java.time.Instant?,
    val elementsCount: Int,
    val errors: List<String> = emptyList(),
)
