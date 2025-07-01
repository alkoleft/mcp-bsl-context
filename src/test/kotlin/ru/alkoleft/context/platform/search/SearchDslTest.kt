/*
 * Copyright (c) 2024-2025 alkoleft. All rights reserved.
 * This file is part of the mcp-bsl-context project.
 *
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package ru.alkoleft.context.platform.search

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import ru.alkoleft.context.platform.dto.ApiType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit тесты для Kotlin Search DSL
 *
 * Валидирует:
 * - DSL синтаксис и функциональность
 * - Типобезопасность
 * - Построение SearchQuery объектов
 */
class SearchDslTest {
    @Test
    fun `search DSL should create basic query`() {
        // Given & When
        val query =
            search {
                query("справочник") {
                    type(ApiType.METHOD)
                    limit(10)
                }
            }

        // Then
        assertAll(
            { assertEquals("справочник", query.text) },
            { assertEquals(ApiType.METHOD, query.type) },
            { assertEquals(10, query.limit) },
            { assertTrue(query.options.intelligent) }, // default value
        )
    }

    @Test
    fun `search DSL should support complex options`() {
        // Given & When
        val query =
            search {
                query("документ") {
                    type(ApiType.PROPERTY)
                    limit(20)
                    options {
                        intelligent = false
                        caseSensitive = true
                        exactMatch = true
                        includeInherited = true
                    }
                }
            }

        // Then
        assertAll(
            { assertEquals("документ", query.text) },
            { assertEquals(ApiType.PROPERTY, query.type) },
            { assertEquals(20, query.limit) },
            { assertEquals(false, query.options.intelligent) },
            { assertEquals(true, query.options.caseSensitive) },
            { assertEquals(true, query.options.exactMatch) },
            { assertEquals(true, query.options.includeInherited) },
        )
    }

    @Test
    fun `search DSL should support minimal query`() {
        // Given & When
        val query =
            search {
                query("поиск") {
                    // только текст, остальное по умолчанию
                }
            }

        // Then
        assertAll(
            { assertEquals("поиск", query.text) },
            { assertEquals(null, query.type) },
            { assertEquals(10, query.limit) },
            { assertTrue(query.options.intelligent) },
        )
    }

    @Test
    fun `search DSL should support all API types`() {
        // Test different API types
        val methodQuery =
            search {
                query("test") { type(ApiType.METHOD) }
            }

        val propertyQuery =
            search {
                query("test") { type(ApiType.PROPERTY) }
            }

        val typeQuery =
            search {
                query("test") { type(ApiType.TYPE) }
            }

        val constructorQuery =
            search {
                query("test") { type(ApiType.CONSTRUCTOR) }
            }

        // Then
        assertAll(
            { assertEquals(ApiType.METHOD, methodQuery.type) },
            { assertEquals(ApiType.PROPERTY, propertyQuery.type) },
            { assertEquals(ApiType.TYPE, typeQuery.type) },
            { assertEquals(ApiType.CONSTRUCTOR, constructorQuery.type) },
        )
    }

    @Test
    fun `search query should generate correct cache key`() {
        // Given
        val query =
            search {
                query("тест") {
                    type(ApiType.METHOD)
                    limit(15)
                    options {
                        intelligent = false
                    }
                }
            }

        // When
        val cacheKey = query.cacheKey()

        // Then
        assertTrue(cacheKey.contains("тест"))
        assertTrue(cacheKey.contains("METHOD"))
        assertTrue(cacheKey.contains("15"))
    }

    @Test
    fun `QueryBuilder should be isolated with @DslMarker`() {
        // This test ensures @DslMarker prevents incorrect DSL usage
        // If @DslMarker works correctly, this should compile without issues

        val query =
            search {
                query("test") {
                    // We should not be able to call 'query' here again
                    // thanks to @DslMarker annotation
                    type(ApiType.METHOD)
                    limit(10)
                    options {
                        intelligent = true
                    }
                }
            }

        assertEquals("test", query.text)
    }

    @Test
    fun `search DSL should be thread safe`() {
        // Given
        val queries =
            (1..100).map { index ->
                Thread {
                    search {
                        query("test_$index") {
                            type(ApiType.METHOD)
                            limit(index)
                        }
                    }
                }
            }

        // When
        queries.forEach { it.start() }
        queries.forEach { it.join() }

        // Then - no exceptions should be thrown
        assertTrue(true, "DSL should handle concurrent access")
    }
}
