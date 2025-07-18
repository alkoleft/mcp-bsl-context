/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import org.junit.jupiter.api.Test
import ru.alkoleft.context.infrastructure.hbk.reader.HbkContentReader
import kotlin.io.path.Path

class HbkReaderTest {
    @Test
    fun read() {
        val platformPath = System.getProperty("platform.context.path")
        val reader = HbkContentReader()
        reader.read(Path(platformPath, "shcntx_ru.hbk")) {
            println("Success")
        }
    }

//    @Test
//    fun readPlatformContextGrabber(@TempDir path: Path){
//        val platformPath = System.getProperty("platform.context.path");
//        val parser = PlatformContextGrabber(Path(platformPath, "shcntx_ru.hbk")), path)
//        parser.parse()
//    }
}
