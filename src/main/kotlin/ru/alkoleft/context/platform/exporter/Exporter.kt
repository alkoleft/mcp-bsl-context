/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.exporter

import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import java.io.IOException
import java.nio.file.Path

interface Exporter {
    @Throws(IOException::class)
    fun writeProperties(
        context: PlatformGlobalContext,
        output: Path,
    )

    @Throws(IOException::class)
    fun writeMethods(
        context: PlatformGlobalContext,
        output: Path,
    )

    @Throws(IOException::class)
    fun writeTypes(
        contexts: List<Context>,
        output: Path,
    )

    fun getExtension(): String
}
