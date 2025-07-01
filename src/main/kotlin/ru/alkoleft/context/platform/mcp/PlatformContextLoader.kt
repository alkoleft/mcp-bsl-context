/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.mcp

import com.github._1c_syntax.bsl.context.PlatformContextGrabber
import com.github._1c_syntax.bsl.context.api.ContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Kotlin реализация компонента для загрузки контекста платформы 1С из файлов справочной системы
 *
 * Основные улучшения:
 * - Kotlin coroutines для асинхронной обработки
 * - Use expressions и scope functions
 * - Null safety и улучшенная обработка ошибок
 * - Extension functions для работы с Path
 * - Автоматическое управление ресурсами
 */
@Component
class PlatformContextLoader {
    companion object {
        private val log = LoggerFactory.getLogger(PlatformContextLoader::class.java)
        private const val CONTEXT_FILE_NAME = "shcntx_ru.hbk"
    }

    /**
     * Загружает контекст платформы из указанного пути
     *
     * @param platformPath путь к каталогу с файлами платформы
     * @return провайдер контекста платформы
     * @throws FileNotFoundException если файл контекста не найден
     * @throws RuntimeException если не удалось загрузить контекст
     */
    fun loadPlatformContext(platformPath: Path): ContextProvider {
        log.info("Загрузка контекста платформы из {}", platformPath)

        val syntaxContextFile =
                findContextFile(platformPath)
                        ?: throw FileNotFoundException("Не удалось найти файл $CONTEXT_FILE_NAME в каталоге $platformPath")

        log.info("Найден файл контекста: {}", syntaxContextFile)

        return Files.createTempDirectory("platform-context").use { tmpDir ->
            try {
                val grabber = PlatformContextGrabber(syntaxContextFile, tmpDir)
                grabber.parse()

                grabber.provider.also {
                    log.info("Контекст платформы успешно загружен")
                }
            } catch (e: Exception) {
                log.error("Ошибка при загрузке контекста платформы", e)
                throw RuntimeException("Не удалось загрузить контекст платформы: ${e.message}", e)
            }
        }
    }

    /**
     * Асинхронная версия загрузки контекста
     */
    suspend fun loadPlatformContextAsync(platformPath: Path): ContextProvider =
            withContext(Dispatchers.IO) {
                loadPlatformContext(platformPath)
            }

    /**
     * Проверяет существование файла контекста в указанном каталоге
     */
    fun hasContextFile(platformPath: Path): Boolean =
            runCatching {
                findContextFile(platformPath) != null
            }.getOrDefault(false)

    /**
     * Ищет файл контекста в указанном каталоге
     */
    private fun findContextFile(path: Path): Path? =
            runCatching {
                Files.walk(path).use { stream ->
                    stream
                            .filter { Files.isRegularFile(it) }
                            .filter { it.fileName.toString() == CONTEXT_FILE_NAME }
                            .findFirst()
                            .orElse(null)
                }
            }.getOrElse { e ->
                log.warn("Ошибка при поиске файла контекста в $path", e)
                null
            }

    /**
     * Extension function для автоматической очистки временного каталога
     */
    private inline fun <T> Path.use(block: (Path) -> T): T =
            try {
                block(this)
            } finally {
                cleanupTempDirectory(this)
            }

    /**
     * Очищает временный каталог с улучшенной обработкой ошибок
     */
    private fun cleanupTempDirectory(tmpDir: Path) {
        runCatching {
            if (Files.exists(tmpDir)) {
                Files.walk(tmpDir).use { stream ->
                    stream
                            .sorted { a, b -> b.compareTo(a) } // Удаляем файлы перед каталогами
                            .forEach { path ->
                                runCatching {
                                    Files.deleteIfExists(path)
                                }.onFailure { e ->
                                    log.warn("Не удалось удалить временный файл: {}", path, e)
                                }
                            }
                }
            }
        }.onFailure { e ->
            log.warn("Ошибка при очистке временного каталога: {}", tmpDir, e)
        }
    }

    /**
     * Получение статистики файла контекста
     */
    fun getContextFileInfo(platformPath: Path): ContextFileInfo? {
        val contextFile = findContextFile(platformPath) ?: return null

        return runCatching {
            ContextFileInfo(
                    path = contextFile,
                    size = Files.size(contextFile),
                    lastModified = Files.getLastModifiedTime(contextFile).toMillis(),
                    exists = Files.exists(contextFile),
            )
        }.getOrNull()
    }

    /**
     * Информация о файле контекста
     */
    data class ContextFileInfo(
            val path: Path,
            val size: Long,
            val lastModified: Long,
            val exists: Boolean,
    ) {
        fun getFormattedSize(): String =
                when {
                    size < 1024 -> "$size B"
                    size < 1024 * 1024 -> "${size / 1024} KB"
                    else -> "${size / (1024 * 1024)} MB"
                }
    }
}
