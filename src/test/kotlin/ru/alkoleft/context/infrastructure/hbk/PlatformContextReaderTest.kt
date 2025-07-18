/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class PlatformContextReaderTest {
    @Test
    fun read() {
        val platformPath = System.getProperty("platform.context.path")
        val reader = PlatformContextReader()
        reader.read(Path(platformPath, "shcntx_ru.hbk")) {
            println("Types: ${types().count()}")
            println("Enums: ${enums().count()}")
            println("Global methods: ${globalMethods().count()}")
            println("Global properties: ${globalProperties().count()}")
        }
    }
}
