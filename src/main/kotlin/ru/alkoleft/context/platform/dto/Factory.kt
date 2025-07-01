/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.dto

import com.github._1c_syntax.bsl.context.api.*
import com.github._1c_syntax.bsl.context.platform.PlatformContextType

object Factory {

    fun methods(context: ContextType): List<MethodDefinition> {
        if (context.methods().isEmpty()) {
            return emptyList()
        }
        return context.methods().map { method(it) }
    }

    fun properties(context: ContextType): List<PropertyDefinition> {
        if (context.properties().isEmpty()) {
            return emptyList()
        }
        return context.properties().map { property(it) }
    }

    fun property(property: ContextProperty): PropertyDefinition {
        return PropertyDefinition(
            name = property.name().name,
            nameEn = property.name().alias ?: "",
            description = property.description() ?: "",
            readonly = property.accessMode() == AccessMode.READ,
            type = returnType(property.types()) ?: ""
        )
    }

    fun method(method: ContextMethod): MethodDefinition {
        val methodSignatures = signatures(method)

        return MethodDefinition(
            name = method.name().name,
            description = method.description() ?: "",
            signature = methodSignatures,
            returnType = returnType(method) ?: ""
        )
    }

    private fun signatures(method: ContextMethod): List<Signature> {
        return method.signatures().map { signature ->
            Signature(
                name = signature.name().alias,
                description = signature.description() ?: "",
                params = signature.parameters().map { parameter(it) }
            )
        }
    }

    fun returnType(method: ContextMethod): String? {
        return returnType(method.returnValues())
    }

    fun returnType(types: List<Context>): String? {
        return if (types.isEmpty()) null else types[0].name().name
    }

    fun parameter(parameter: ContextSignatureParameter): ParameterDefinition {
        return ParameterDefinition(
            required = parameter.isRequired,
            name = parameter.name().name,
            description = parameter.description() ?: "",
            type = returnType(parameter.types()) ?: ""
        )
    }

    fun constructors(context: PlatformContextType): List<ISignature> {
        if (context.constructors().isEmpty()) {
            return emptyList()
        }
        return context.constructors().map { constructor ->
            Signature(
                name = constructor.name().alias,
                description = constructor.description() ?: "",
                params = constructor.parameters().map { parameter(it) }
            )
        }
    }
} 