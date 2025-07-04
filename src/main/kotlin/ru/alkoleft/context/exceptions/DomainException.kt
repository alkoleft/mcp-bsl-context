/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.exceptions

/**
 * Базовое исключение доменного слоя
 */
abstract class DomainException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

/**
 * Исключение при поиске типа платформы
 */
class PlatformTypeNotFoundException(
    typeName: String,
) : DomainException("**Тип не найден:** $typeName")

/**
 * Исключение при невалидном поисковом запросе
 */
class InvalidSearchQueryException(
    message: String,
) : DomainException("Невалидный поисковый запрос: $message")

/**
 * Исключение при ошибке загрузки контекста платформы
 */
class PlatformContextLoadException(
    message: String,
    cause: Throwable? = null,
) : DomainException("Ошибка загрузки контекста платформы: $message", cause)

/**
 * Исключение при поиске типов
 */
class TypeNotFoundException(
    typeName: String,
) : DomainException("Тип '$typeName' не найден")

/**
 * Исключение при поиске типов
 */
class TypeMemberNotFoundException(
    memberName: String,
    typeName: String,
) : DomainException("Элемент '$memberName' в типе '$typeName' не найден")

/**
 * Исключение при поиске типов
 */
class DefinitionNotFoundException(
    entityName: String,
    typeName: String,
) : DomainException("Элемент '$entityName' с типов '$typeName' не найден")

/**
 * Исключение при ошибке загрузки данных
 */
class DataLoadException(
    message: String,
    cause: Throwable? = null,
) : DomainException("Ошибка загрузки данных: $message", cause)

/**
 * Исключение при превышении лимита результатов
 */
class ResultLimitExceededException(
    limit: Int,
) : DomainException("Превышен лимит результатов: $limit")
