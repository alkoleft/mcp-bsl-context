/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.infrastructure.adapters.outgoing.repositories.mappers

import org.springframework.stereotype.Component
import ru.alkoleft.context.core.domain.api.Constructor
import ru.alkoleft.context.core.domain.api.DataSource
import ru.alkoleft.context.core.domain.api.Method
import ru.alkoleft.context.core.domain.api.MethodSignature
import ru.alkoleft.context.core.domain.api.Parameter
import ru.alkoleft.context.core.domain.api.Property
import ru.alkoleft.context.core.domain.api.Type
import ru.alkoleft.context.core.domain.api.TypeReference
import ru.alkoleft.context.platform.dto.ISignature
import ru.alkoleft.context.platform.dto.MethodDefinition
import ru.alkoleft.context.platform.dto.ParameterDefinition
import ru.alkoleft.context.platform.dto.PlatformTypeDefinition
import ru.alkoleft.context.platform.dto.PropertyDefinition
import ru.alkoleft.context.platform.dto.Signature

/**
 * Маппер для преобразования старых DTO в новые доменные модели
 * Обеспечивает совместимость с существующим кодом
 */
@Component
class DomainModelMapper {
    /**
     * Преобразование PlatformTypeDefinition в доменную модель Type
     */
    fun mapPlatformTypeToType(dto: PlatformTypeDefinition): Type {
        val typeRef = TypeReference(dto.name, dto.name)

        val methods =
            dto.methods.map { methodDto ->
                mapMethodDefinitionToMethod(methodDto, isGlobal = false, parentType = typeRef)
            }

        val properties =
            dto.properties.map { propertyDto ->
                mapPropertyDefinitionToProperty(propertyDto, parentType = typeRef)
            }

        val constructors =
            dto.constructors.map { constructorDto ->
                mapSignatureToConstructor(constructorDto, parentType = typeRef)
            }

        return Type(
            name = dto.name,
            description = dto.description,
            source = DataSource.BSL_CONTEXT,
            methods = methods,
            properties = properties,
            constructors = constructors,
        )
    }

    /**
     * Преобразование MethodDefinition в доменную модель Method
     */
    fun mapMethodDefinitionToMethod(
        dto: MethodDefinition,
        isGlobal: Boolean = false,
        parentType: TypeReference? = null,
    ): Method {
        val signatures =
            dto.signature.map { signatureDto ->
                mapSignatureToMethodSignature(signatureDto)
            }

        val returnType =
            if (dto.returnType.isNotBlank()) {
                TypeReference(dto.returnType)
            } else {
                null
            }

        return Method(
            name = dto.name,
            description = dto.description,
            source = DataSource.BSL_CONTEXT,
            signatures = signatures,
            returnType = returnType,
            isGlobal = isGlobal,
            parentType = parentType,
        )
    }

    /**
     * Преобразование PropertyDefinition в доменную модель Property
     */
    fun mapPropertyDefinitionToProperty(
        dto: PropertyDefinition,
        parentType: TypeReference? = null,
    ): Property =
        Property(
            name = dto.name,
            description = dto.description,
            source = DataSource.BSL_CONTEXT,
            dataType = TypeReference(dto.type),
            isReadonly = dto.readonly,
            parentType = parentType,
        )

    /**
     * Преобразование Signature в MethodSignature
     */
    fun mapSignatureToMethodSignature(dto: Signature): MethodSignature {
        val parameters =
            dto.params.map { paramDto ->
                mapParameterDefinitionToParameter(paramDto)
            }

        return MethodSignature(
            parameters = parameters,
            returnType = null, // Return type определяется на уровне MethodDefinition, не Signature
            isDeprecated = false, // Информация о deprecated не доступна в текущих DTO
        )
    }

    /**
     * Преобразование ISignature в Constructor
     */
    fun mapSignatureToConstructor(
        dto: ISignature,
        parentType: TypeReference,
    ): Constructor {
        // Для конструкторов используем имя типа
        val constructorName = "Новый ${parentType.name}"

        val parameters =
            when (dto) {
                is Signature ->
                    dto.params.map { paramDto ->
                        mapParameterDefinitionToParameter(paramDto)
                    }

                else -> emptyList() // Обработка других типов ISignature при необходимости
            }

        return Constructor(
            name = constructorName,
            description = "Конструктор типа ${parentType.name}",
            source = DataSource.BSL_CONTEXT,
            parameters = parameters,
            parentType = parentType,
        )
    }

    /**
     * Преобразование ParameterDefinition в Parameter
     */
    private fun mapParameterDefinitionToParameter(dto: ParameterDefinition): Parameter =
        Parameter(
            name = dto.name,
            type = TypeReference(dto.type),
            isOptional = !dto.required, // Инвертируем required для получения optional
            defaultValue = null, // Информация о значении по умолчанию не доступна
            description = dto.description,
        )

    /**
     * Обратное преобразование - из доменной модели в DTO (если потребуется)
     */
    fun mapTypeToSearchResults(types: List<Type>): ru.alkoleft.context.platform.dto.SearchResults {
        val typeDtos =
            types.map { type ->
                PlatformTypeDefinition(
                    name = type.name,
                    description = type.description,
                    methods =
                        type.methods.map { method ->
                            mapMethodToMethodDefinition(method)
                        },
                    properties =
                        type.properties.map { property ->
                            mapPropertyToPropertyDefinition(property)
                        },
                    constructors =
                        type.constructors.map { constructor ->
                            mapConstructorToSignature(constructor)
                        },
                )
            }

        return ru.alkoleft.context.platform.dto.SearchResults(
            types = typeDtos,
        )
    }

    /**
     * Преобразование Method обратно в MethodDefinition
     */
    private fun mapMethodToMethodDefinition(method: Method): MethodDefinition {
        val signatures =
            method.signatures.map { signature ->
                Signature(
                    name = method.name,
                    description = method.description,
                    params =
                        signature.parameters.map { param ->
                            ParameterDefinition(
                                name = param.name,
                                type = param.type.name,
                                required = !param.isOptional,
                                description = param.description,
                            )
                        },
                )
            }

        return MethodDefinition(
            name = method.name,
            description = method.description,
            signature = signatures,
            returnType = method.returnType?.name ?: "",
        )
    }

    /**
     * Преобразование Property обратно в PropertyDefinition
     */
    private fun mapPropertyToPropertyDefinition(property: Property): PropertyDefinition =
        PropertyDefinition(
            name = property.name,
            nameEn = property.name, // Используем то же имя для English
            description = property.description,
            readonly = property.isReadonly,
            type = property.dataType.name,
        )

    /**
     * Преобразование Constructor обратно в Signature
     */
    private fun mapConstructorToSignature(constructor: Constructor): Signature =
        Signature(
            name = constructor.name,
            description = constructor.description,
            params =
                constructor.parameters.map { param ->
                    ParameterDefinition(
                        name = param.name,
                        type = param.type.name,
                        required = !param.isOptional,
                        description = param.description,
                    )
                },
        )
}
