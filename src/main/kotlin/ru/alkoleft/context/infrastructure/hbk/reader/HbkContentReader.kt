/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk.reader

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import ru.alkoleft.context.exceptions.PlatformContextLoadException
import ru.alkoleft.context.infrastructure.hbk.models.Page
import ru.alkoleft.context.infrastructure.hbk.toc.Toc
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipInputStream

private const val PACK_BLOCK_NAME = "PackBlock"
private const val FILE_STORAGE_NAME = "FileStorage"

private val logger = KotlinLogging.logger { }

/**
 * Читает содержимое HBK файлов и предоставляет доступ к их структуре.
 *
 * Этот класс является основным интерфейсом для работы с содержимым HBK файлов.
 * Он извлекает оглавление (Table of Contents) и файловое хранилище из HBK контейнера,
 * предоставляя доступ к HTML страницам документации через ZIP-архив.
 *
 * Основные возможности:
 * - Извлечение оглавления (TOC) из сжатого блока PackBlock
 * - Доступ к HTML файлам документации через ZIP-архив
 * - Предоставление контекста для парсинга страниц
 *
 * @see HbkContainerReader для извлечения данных из HBK контейнера
 * @see Toc для работы с оглавлением
 * @see ru.alkoleft.context.infrastructure.hbk.PlatformContextReader для полного процесса чтения контекста
 */
class HbkContentReader {
    /**
     * Читает HBK файл и выполняет блок кода с контекстом.
     *
     * @param path Путь к HBK файлу
     * @param block Блок кода, выполняемый с контекстом доступа к содержимому
     */
    fun read(
        path: Path,
        block: Context.() -> Unit,
    ) {
        val extractor = HbkContainerReader()

        extractor.readHbk(path) {
            val toc = Toc.parse(getInflatePackBlock(getEntity(PACK_BLOCK_NAME) as ByteArray))
            val fileStorage = getEntity(FILE_STORAGE_NAME)

            SeekableInMemoryByteChannel(fileStorage).use {
                val zip =
                    ZipFile
                        .builder()
                        .setSeekableByteChannel(it)
                        .get()
                zip.use { file ->
                    val context = Context(toc, file)
                    context.apply(block)
                }
            }
        }
    }

    /**
     * Контекст для работы с содержимым HBK файла.
     *
     * Предоставляет доступ к оглавлению и ZIP-архиву с HTML файлами документации.
     *
     * @property toc Оглавление HBK файла
     * @property zipFile ZIP-архив с HTML файлами документации
     */
    class Context(
        val toc: Toc,
        private val zipFile: ZipFile,
    ) {
        /**
         * Получает поток для чтения HTML файла страницы.
         *
         * @param page Страница документации
         * @return Поток для чтения HTML содержимого
         * @throws PlatformContextLoadException если файл не найден или имя не указано
         */
        fun getEntryStream(page: Page) = getEntryStream(page.htmlPath)

        /**
         * Получает поток для чтения HTML файла по имени.
         *
         * @param name Имя HTML файла в архиве
         * @return Поток для чтения HTML содержимого
         * @throws PlatformContextLoadException если файл не найден или имя не указано
         */
        fun getEntryStream(name: String): InputStream {
            if (name.isEmpty()) {
                throw PlatformContextLoadException("Не указано имя файла для поиска в архиве")
            }
            val validName =
                if (name.startsWith("/")) {
                    name.substring(1)
                } else {
                    name
                }
            val entry = zipFile.getEntry(validName)
            return if (entry != null) {
                zipFile.getInputStream(entry)
            } else {
                throw PlatformContextLoadException("Не найден файл в архиве $name")
            }
        }
    }
}

private fun getInflatePackBlock(data: ByteArray): ByteArray {
    val inflateData: ByteArray

    val buffer = ByteArray(2048)

    try {
        ZipInputStream(ByteArrayInputStream(data)).use { stream ->
            stream.getNextEntry()
            inflateData = stream.readAllBytes()
        }
    } catch (ex: IOException) {
        throw RuntimeException(ex)
    }

    return inflateData
}
