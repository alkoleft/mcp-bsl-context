/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.exporter

import com.github._1c_syntax.bsl.context.api.AccessMode
import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.api.ContextMethodSignature
import com.github._1c_syntax.bsl.context.platform.PlatformContextType
import com.github._1c_syntax.bsl.context.platform.PlatformGlobalContext
import org.springframework.stereotype.Service
import ru.alkoleft.context.platform.dto.Factory
import ru.alkoleft.context.platform.dto.MethodDefinition
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import ru.alkoleft.context.platform.dto.PropertyDefinition
import ru.alkoleft.context.platform.dto.Signature
import java.util.Objects
import java.util.Optional
import java.util.stream.Stream

/**
 * Base implementation for exporting platform context data.
 * Provides test data for 1C platform methods and properties demonstration.
 */
@Service
class BaseExporterLogic : ExporterLogic {
    companion object {
        private const val PRIMARY_SIGNATURE_NAME = "Основной"
    }

    override fun extractProperties(context: PlatformGlobalContext): Stream<PropertyDefinition> {
        Objects.requireNonNull(context, "PlatformGlobalContext cannot be null")

        return Optional
            .ofNullable(context.properties())
            .map { it.stream() }
            .orElse(Stream.empty())
            .map { property ->
                val type =
                    Optional
                        .ofNullable(property.types())
                        .filter { types -> types.isNotEmpty() }
                        .map { types -> types[0].name().name }
                        .orElse(null)

                PropertyDefinition(
                    name = property.name().name,
                    nameEn = property.name().alias ?: "",
                    description = property.description() ?: "",
                    readonly = AccessMode.READ == property.accessMode(),
                    type = type ?: "",
                )
            }
    }

    override fun extractMethods(context: PlatformGlobalContext): Stream<MethodDefinition> {
        Objects.requireNonNull(context, "PlatformGlobalContext cannot be null")

        return Optional
            .ofNullable(context.methods())
            .stream()
            .flatMap { it.stream() }
            .map { method ->
                val signatures =
                    Optional
                        .ofNullable(method.signatures())
                        .map { sigs ->
                            sigs.map { toSignature(it) }
                        }.orElse(emptyList())

                val returnValue =
                    if (method.hasReturnValue() &&
                        method.returnValues() != null &&
                        method.returnValues().isNotEmpty()
                    ) {
                        method.returnValues()[0].name().name
                    } else {
                        null
                    }

                MethodDefinition(
                    name = method.name().name,
                    description = method.description() ?: "",
                    signature = signatures,
                    returnType = returnValue ?: "",
                )
            }
    }

    private fun toSignature(sig: ContextMethodSignature): Signature {
        val sigDescription =
            if (PRIMARY_SIGNATURE_NAME == sig.name().name) {
                sig.description()
            } else {
                "${sig.name().name}. ${sig.description()}"
            }

        val paramsList =
            Optional
                .ofNullable(sig.parameters())
                .filter { params -> params.isNotEmpty() }
                .map { params ->
                    params.map { Factory.parameter(it) }
                }.orElse(emptyList())

        return Signature(
            name = sig.name().alias,
            description = sigDescription,
            params = paramsList,
        )
    }

    override fun extractTypes(contexts: List<Context>): Stream<PlatformTypeDefinition> =
        Optional
            .ofNullable(contexts)
            .stream()
            .flatMap { it.stream() }
            .filter { it is PlatformContextType }
            .map { it as PlatformContextType }
            .map { createTypeDefinition(it) }

    private fun createTypeDefinition(context: PlatformContextType): PlatformTypeDefinition =
        PlatformTypeDefinition(
            name = context.name().name,
            description = "",
            methods = Factory.methods(context),
            properties = Factory.properties(context),
            constructors = Factory.constructors(context),
        )
}
