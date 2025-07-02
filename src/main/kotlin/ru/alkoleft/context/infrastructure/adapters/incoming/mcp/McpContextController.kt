/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.incoming.mcp

import org.slf4j.LoggerFactory
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Service
import ru.alkoleft.context.core.ports.incoming.ContextUseCase
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Тонкий MCP адаптер для управления контекстом платформы
 * Предоставляет инструменты для загрузки и проверки статуса контекста
 */
@Service
class McpContextController(
    private val contextUseCase: ContextUseCase,
) {
    companion object {
        private val log = LoggerFactory.getLogger(McpContextController::class.java)
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }

    /**
     * Загрузка контекста платформы из указанного пути
     */
    @Tool(
        name = "loadContext",
        description = "Загрузка контекста API платформы 1С из указанного пути. Требуется для инициализации поиска.",
    )
    suspend fun loadContext(
        @ToolParam(description = "Путь к файлам платформы 1С или директории с контекстом")
        path: String,
        @ToolParam(description = "Принудительная перезагрузка контекста (по умолчанию false)")
        forceReload: Boolean = false,
    ): String {
        if (path.isBlank()) {
            return "❌ **Ошибка:** Путь не может быть пустым"
        }

        try {
            val context =
                if (forceReload) {
                    contextUseCase.refreshContext()
                } else {
                    contextUseCase.loadContext(path)
                }

            log.info("Context loaded successfully from: {}", path)

            return buildString {
                appendLine("✅ **Контекст загружен успешно**")
                appendLine()
                appendLine("📁 **Путь:** `$path`")
                appendLine("📊 **Элементов загружено:** ${context.totalElements}")
                appendLine("📅 **Время загрузки:** ${context.loadedAt.atZone(ZoneId.systemDefault()).format(dateFormatter)}")
                appendLine("🔧 **Источники данных:** ${context.sources.joinToString()}")
                context.version?.let {
                    appendLine("📋 **Версия:** $it")
                }
                appendLine()
                appendLine("🔍 Теперь вы можете использовать поиск по API платформы!")
            }
        } catch (e: Exception) {
            log.error("Context loading error for path: $path", e)
            return "❌ **Ошибка загрузки контекста:** ${e.message}"
        }
    }

    /**
     * Получение статуса текущего контекста
     */
    @Tool(
        name = "contextStatus",
        description = "Проверка статуса загруженного контекста платформы 1С и доступности поиска.",
    )
    suspend fun getContextStatus(): String {
        try {
            val status = contextUseCase.getContextStatus()

            return buildString {
                if (status.isLoaded) {
                    appendLine("✅ **Контекст загружен и готов к работе**")
                    appendLine()
                    appendLine("📊 **Элементов в контексте:** ${status.elementsCount}")
                    status.lastUpdate?.let {
                        appendLine("📅 **Последнее обновление:** ${it.atZone(ZoneId.systemDefault()).format(dateFormatter)}")
                    }

                    if (status.errors.isNotEmpty()) {
                        appendLine()
                        appendLine("⚠️ **Предупреждения:**")
                        status.errors.forEach { error ->
                            appendLine("- $error")
                        }
                    }
                } else {
                    appendLine("❌ **Контекст не загружен**")
                    appendLine()
                    appendLine("💡 Используйте инструмент `loadContext` для загрузки контекста платформы.")

                    if (status.errors.isNotEmpty()) {
                        appendLine()
                        appendLine("🔴 **Ошибки загрузки:**")
                        status.errors.forEach { error ->
                            appendLine("- $error")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Status check error", e)
            return "❌ **Ошибка проверки статуса:** ${e.message}"
        }
    }

    /**
     * Обновление текущего контекста
     */
    @Tool(
        name = "refreshContext",
        description = "Обновление загруженного контекста платформы 1С. Перезагружает данные из источника.",
    )
    suspend fun refreshContext(): String {
        try {
            val context = contextUseCase.refreshContext()

            log.info("Context refreshed successfully")

            return buildString {
                appendLine("🔄 **Контекст обновлен успешно**")
                appendLine()
                appendLine("📊 **Элементов загружено:** ${context.totalElements}")
                appendLine("📅 **Время обновления:** ${context.loadedAt.atZone(ZoneId.systemDefault()).format(dateFormatter)}")
                appendLine("🔧 **Источники данных:** ${context.sources.joinToString()}")
                context.version?.let {
                    appendLine("📋 **Версия:** $it")
                }
                appendLine()
                appendLine("✅ Контекст готов к использованию!")
            }
        } catch (e: Exception) {
            log.error("Context refresh error", e)
            return "❌ **Ошибка обновления контекста:** ${e.message}"
        }
    }

    /**
     * Получение списка доступных источников данных
     */
    @Tool(
        name = "getDataSources",
        description = "Получение списка доступных источников данных для платформы 1С.",
    )
    suspend fun getDataSources(): String {
        try {
            val sources = contextUseCase.getAvailableDataSources()

            return buildString {
                appendLine("📋 **Доступные источники данных:**")
                appendLine()
                sources.forEach { source ->
                    appendLine("- **${source.name}**: ${getSourceDescription(source.name)}")
                }
                appendLine()
                appendLine("💡 В текущей версии поддерживается автоматическая загрузка из BSL_CONTEXT.")
            }
        } catch (e: Exception) {
            log.error("Data sources retrieval error", e)
            return "❌ **Ошибка получения источников:** ${e.message}"
        }
    }

    /**
     * Получение описания источника данных
     */
    private fun getSourceDescription(sourceName: String): String =
        when (sourceName) {
            "BSL_CONTEXT" -> "Основной источник API данных платформы"
            "CONFIGURATION" -> "Метаданные конфигурации (планируется)"
            "EXTENSION" -> "Расширения конфигурации (планируется)"
            "DOCUMENTATION" -> "Документация платформы (планируется)"
            else -> "Неизвестный источник"
        }
}
