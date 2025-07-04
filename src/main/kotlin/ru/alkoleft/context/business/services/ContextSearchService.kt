/*
 * Copyright (c) 2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.business.services

import org.springframework.stereotype.Service
import ru.alkoleft.context.business.entities.Definition
import ru.alkoleft.context.business.entities.Signature
import ru.alkoleft.context.business.persistent.PlatformContextRepository
import ru.alkoleft.context.business.valueobjects.ApiType
import ru.alkoleft.context.exceptions.InvalidSearchQueryException
import ru.alkoleft.context.exceptions.PlatformTypeNotFoundException
import ru.alkoleft.context.exceptions.TypeMemberNotFoundException

@Service
class ContextSearchService(
    private val repository: PlatformContextRepository,
) {
    fun searchAll(
        query: String,
        type: String? = null,
        limit: Int? = null,
    ): List<Definition> {
        if (query.isBlank()) {
            throw InvalidSearchQueryException("Запрос не может быть пустым")
        }
        val effectiveLimit = limit?.coerceIn(1, 50) ?: 10
        return repository.search(query, effectiveLimit, getApiType(type))
    }

    fun getInfo(
        name: String,
        type: String?,
    ): Definition? {
        if (name.isBlank() || type.isNullOrBlank()) {
            throw InvalidSearchQueryException("Имя элемента и тип элемента не могут быть пустыми")
        }

        val apiType = getApiType(type)
        return when (apiType) {
            ApiType.TYPE -> repository.findType(name)
            ApiType.PROPERTY -> repository.findProperty(name)
            ApiType.METHOD -> repository.findMethod(name)
            else -> throw InvalidSearchQueryException("Получение информации для типа '$type' не поддерживается")
        }
    }

    fun findMemberByTypeAndName(
        typeName: String,
        memberName: String,
    ): Definition {
        if (typeName.isBlank() || memberName.isBlank()) {
            throw InvalidSearchQueryException("Имя типа и имя элемента не могут быть пустыми")
        }

        val type = repository.findType(typeName)
        if (type == null) {
            throw PlatformTypeNotFoundException(typeName)
        }

        return repository.findTypeMember(type, memberName)
            ?: throw TypeMemberNotFoundException(memberName, typeName)
    }

    fun findTypeMembers(typeName: String): List<Definition> {
        if (typeName.isBlank()) {
            throw InvalidSearchQueryException("Имя типа не может быть пустым")
        }
        val type = repository.findType(typeName)

        return if (type != null) {
            type.methods + type.properties
        } else {
            throw PlatformTypeNotFoundException(typeName)
        }
    }

    fun findConstructors(typeName: String): List<Signature> {
        if (typeName.isBlank()) {
            throw InvalidSearchQueryException("Имя типа не может быть пустым")
        }
        val type = repository.findType(typeName)

        return type?.constructors ?: throw PlatformTypeNotFoundException(typeName)
    }

    fun getApiType(type: String?): ApiType? {
        if (type.isNullOrBlank()) return null
        return ApiType.getType(type) ?: throw InvalidSearchQueryException("Неизвестный тип '$type' искомого элемента")
    }
}
