/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.persistent.storage

import com.github._1c_syntax.bsl.context.platform.PlatformContextType
import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class PlatformContextStorage(
    private val loader: PlatformContextLoader,
) {
    var methods: List<MethodDefinition> = emptyList()
    var properties: List<PropertyDefinition> = emptyList()
    var types: List<PlatformTypeDefinition> = emptyList()
    private val indexInitialized = AtomicBoolean(false)
    private val lock = ReentrantReadWriteLock()

    fun load() {
        // Быстрая проверка с read lock
        lock.read {
            if (indexInitialized.get()) {
                return
            }
        }

        // Инициализация с write lock
        return lock.write {
            // Двойная проверка в write lock
            if (indexInitialized.get()) {
                return
            } else {
                loadPlatformContext()
            }
        }
    }

    private fun loadPlatformContext() {
        if (indexInitialized.get()) {
            return
        }
        val provider =
            loader.loadPlatformContext(
                Path
                    .of(""),
            )
        methods = provider.globalContext.methods().map { it.toEntity() }
        properties = provider.globalContext.properties().map { it.toEntity() }
        types =
            provider.contexts
                .filterIsInstance<PlatformContextType>()
                .map { it.toEntity() }
    }
}
