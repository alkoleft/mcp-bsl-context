/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.hbk

import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class HbkReaderTest {
    @Test
    fun read() {
        val reader = HbkContentReader()
        reader.read(Path("/opt/1cv8/x86_64/8.3.27.1326/shcntx_ru.hbk")) {
            println("Success")
        }
    }

//    @Test
//    fun readPlatformContextGrabber(@TempDir path: Path){
//        val parser = PlatformContextGrabber(Path("/opt/1cv8/x86_64/8.3.27.1326/shcntx_ru.hbk"), path)
//        parser.parse()
//    }
}
