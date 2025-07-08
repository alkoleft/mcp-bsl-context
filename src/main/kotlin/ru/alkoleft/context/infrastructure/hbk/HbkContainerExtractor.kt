/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

// За основу взято https://github.com/1c-syntax/bsl-context/blob/rnd/src/main/java/com/github/_1c_syntax/bsl/context/platform/hbk/HbkContainerExtractor.java
package ru.alkoleft.context.infrastructure.hbk

import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.Path

private const val BYTES_BY_FILE_INFOS = 12 // int * 4

class HbkContainerExtractor {
    inner class EntitiesScope(
        val entities: Map<String, Int>,
        val buffer: MappedByteBuffer,
    ) {
        fun getEntity(name: String): ByteArray? = entities[name]?.let { getHbkFileBody(buffer, it) }
    }

    fun readHbk(
        path: Path,
        block: EntitiesScope.() -> Unit,
    ) {
        if (!path.toFile().exists()) {
            throw IllegalArgumentException("Hbk-file not exists")
        }
        FileInputStream(path.toFile()).use { stream ->
            val channel = stream.channel
            val buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            val entities = entities(buffer)
            EntitiesScope(entities, buffer).apply(block)
        }
    }

    fun entities(buffer: ByteBuffer): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        skipBlock(buffer, 16) // int * 4

        skipBlock(buffer, 2) // short
        val payloadSize = getLongString(buffer)
        val blockSize = getLongString(buffer)
        skipBlock(buffer, 11) // long + byte + short

        val position = buffer.position()

        val fileInfos = ByteArray(payloadSize)
        buffer.get(fileInfos)

        buffer.position(position + blockSize)

        val remainingBuffer = ByteBuffer.wrap(fileInfos).order(ByteOrder.LITTLE_ENDIAN)
        val count = remainingBuffer.capacity() / BYTES_BY_FILE_INFOS

        // 559
        for (i in 0 until count) {
            val headerAddress = remainingBuffer.int
            val bodyAddress = remainingBuffer.int
            val reserved = remainingBuffer.int
            if (reserved != Int.MAX_VALUE) {
                throw RuntimeException()
            }

            val name = getHbkFileName(buffer, headerAddress)
            result[name] = bodyAddress
        }
        return result.toMap()
    }

    private fun getHbkFileName(
        buffer: ByteBuffer,
        headerAddress: Int,
    ): String {
        buffer.position(headerAddress)

        skipBlock(buffer, 2)
        val payloadSize = getLongString(buffer) // + 8 + 1
        skipBlock(buffer, 40) // 8 + 1 + 8 + 1 + 2 + 8 + 8 + 4

        val stringArray = ByteArray(payloadSize - 24) // int * 6, которые пропускаем
        buffer.get(stringArray)

        return String(stringArray, StandardCharsets.UTF_16LE)
    }

    private fun getHbkFileBody(
        outBuffer: ByteBuffer,
        bodyAddress: Int,
    ): ByteArray {
        outBuffer.position(bodyAddress)

        skipBlock(outBuffer, 2)
        val payloadSize = getLongString(outBuffer)

        skipBlock(outBuffer, 20) // long * 2 + int * 2 + short

        // получим буффер, позици и размер для чтения тела
        val rawData = ByteArray(payloadSize) // blockSize
        outBuffer.get(rawData)

        return rawData
    }

    private fun getLongString(buffer: ByteBuffer): Int {
        val stringBuffer = ByteArray(8)
        buffer.get(stringBuffer)
        buffer.get()
        return String(stringBuffer).toLong(16).toInt()
    }

    private fun skipBlock(
        buffer: ByteBuffer,
        size: Int,
    ) {
        buffer.position(buffer.position() + size)
    }
}
