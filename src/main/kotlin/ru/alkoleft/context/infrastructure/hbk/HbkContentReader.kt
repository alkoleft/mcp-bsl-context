/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import ru.alkoleft.context.exceptions.PlatformContextLoadException
import ru.alkoleft.context.infrastructure.hbk.toc.Toc
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipInputStream

private const val PACK_BLOCK_NAME = "PackBlock"
private const val FILE_STORAGE_NAME = "FileStorage"

private val logger = KotlinLogging.logger { }

class HbkContentReader {
    fun read(path: Path, block: Context.() -> Unit) {
        val extractor = HbkContainerExtractor()

        extractor.readHbk(path) {
            val toc = Toc.parse(getInflatePackBlock(getEntity(PACK_BLOCK_NAME) as ByteArray))
            val fileStorage = getEntity(FILE_STORAGE_NAME)

            SeekableInMemoryByteChannel(fileStorage).use {
                val zip = ZipFile
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

    class Context(val toc: Toc, val zipFile: ZipFile) {
        fun getEntryStream(page: Page) = getEntryStream(page.htmlPath)
        fun getEntryStream(name: String): InputStream {
            if (name.isEmpty()) {
                throw PlatformContextLoadException("Не указано имя файла для поиска в архиве")
            }
            val validName =
                if (name.startsWith("/")) name.substring(1)
                else name
            val entry = zipFile.getEntry(validName)
            return if (entry != null) {
                zipFile.getInputStream(entry)
            } else {
                throw PlatformContextLoadException("Не найден файл в архиве $name")
            }
        }
    }

    private fun walk(zip: ZipFile, pages: List<Page>) {
        pages.forEach { page ->
            if (page.htmlPath.isNotBlank()) {
                zip.getEntries(page.htmlPath).forEach {
                    logger.info { "${page.title} - ${it.name}" }
                }

            }
            if (page.children.isNotEmpty()) {
                walk(zip, page.children)
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
