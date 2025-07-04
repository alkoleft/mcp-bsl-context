/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.persistent.storage

import com.github._1c_syntax.bsl.context.PlatformContextGrabber
import com.github._1c_syntax.bsl.context.api.ContextProvider
import org.slf4j.LoggerFactory
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.valueobjects.ApiType
import ru.alkoleft.context.exceptions.PlatformContextLoadException
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Infrastructure External Service: Загрузчик контекста платформы 1С
 *
 * Адаптер для работы с HBK библиотекой для загрузки контекста платформы
 */
class PlatformContextLoader {
    companion object {
        private val log = LoggerFactory.getLogger(PlatformContextLoader::class.java)
        private const val CONTEXT_FILE_NAME = "shcntx_ru.hbk"
    }

    /**
     * Загружает все типы платформы
     */
    suspend fun loadAllTypes(): List<PlatformTypeDefinition> {
        // TODO: Реализовать загрузку из HBK
        return emptyList()
    }

    /**
     * Загружает тип по имени
     */
    suspend fun loadTypeByName(name: String): PlatformTypeDefinition? {
        // TODO: Реализовать загрузку из HBK
        return null
    }

    /**
     * Загружает типы по типу API
     */
    suspend fun loadTypesByApiType(apiType: ApiType): List<PlatformTypeDefinition> {
        // TODO: Реализовать загрузку из HBK
        return emptyList()
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
                throw PlatformContextLoadException("Не удалось загрузить контекст платформы: ${e.message}", e)
            }
        }
    }

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
}
