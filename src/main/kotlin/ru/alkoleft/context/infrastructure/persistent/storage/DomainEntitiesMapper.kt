/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.persistent.storage

import com.github._1c_syntax.bsl.context.api.Context
import com.github._1c_syntax.bsl.context.api.ContextConstructor
import com.github._1c_syntax.bsl.context.api.ContextMethod
import com.github._1c_syntax.bsl.context.api.ContextMethodSignature
import com.github._1c_syntax.bsl.context.api.ContextProperty
import com.github._1c_syntax.bsl.context.api.ContextSignatureParameter
import com.github._1c_syntax.bsl.context.platform.PlatformContextType
import ru.alkoleft.context.business.entities.MethodDefinition
import ru.alkoleft.context.business.entities.ParameterDefinition
import ru.alkoleft.context.business.entities.PlatformTypeDefinition
import ru.alkoleft.context.business.entities.PropertyDefinition
import ru.alkoleft.context.business.entities.Signature

private const val PRIMARY_SIGNATURE_NAME = "Основной"

fun ContextProperty.toEntity() =
    PropertyDefinition(
        name = name().name,
        description = description(),
        propertyType = types()[0].name().name,
    )

fun ContextMethod.toEntity() =
    MethodDefinition(
        name = name().name,
        description = description() ?: "",
        signature = signatures().map { it.toEntity() },
        returnType = returnValue(this),
    )

fun ContextMethodSignature.toEntity() =
    Signature(
        name = name().alias,
        description = signatureDescription(this),
        parameters = parameters().map { it.toEntity() },
    )

fun ContextSignatureParameter.toEntity(): ParameterDefinition =
    ParameterDefinition(
        required = isRequired,
        name = name().name,
        description = description() ?: "",
        type = typePresentation(types()),
    )

fun ContextConstructor.toEntity() =
    Signature(
        name = name().alias,
        description = description() ?: "",
        parameters = parameters().map { it.toEntity() },
    )

fun PlatformContextType.toEntity() =
    PlatformTypeDefinition(
        name = name().name,
        description = "",
        methods = methods.map { it.toEntity() },
        properties = properties.map { it.toEntity() },
        constructors = constructors.map { it.toEntity() },
    )

fun returnValue(method: ContextMethod): String =
    if (method.hasReturnValue() &&
        method.returnValues() != null &&
        method.returnValues().isNotEmpty()
    ) {
        method.returnValues()[0].name().name
    } else {
        ""
    }

fun signatureDescription(sig: ContextMethodSignature): String =
    if (PRIMARY_SIGNATURE_NAME == sig.name().name) {
        sig.description()
    } else {
        "${sig.name().name}. ${sig.description()}"
    }

fun typePresentation(types: List<Context>): String = if (types.isEmpty()) "" else types[0].name().name
