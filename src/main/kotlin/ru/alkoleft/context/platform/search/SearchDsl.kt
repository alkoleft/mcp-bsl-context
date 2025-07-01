package ru.alkoleft.context.platform.search

import ru.alkoleft.context.platform.dto.ApiType
import ru.alkoleft.context.platform.dto.SearchOptions
import ru.alkoleft.context.platform.dto.SearchQuery

/**
 * DSL marker аннотация для предотвращения неправильного использования DSL
 */
@DslMarker
annotation class SearchDsl

/**
 * Основной контекст для Search DSL
 *
 * Реализует Context DSL с Scope Functions согласно творческому решению
 */
@SearchDsl
class SearchContext {

    /**
     * Создание поискового запроса с типобезопасным DSL
     */
    fun query(text: String, block: QueryBuilder.() -> Unit): SearchQuery {
        return QueryBuilder(text).apply(block).build()
    }
}

/**
 * Builder для построения поискового запроса
 */
@SearchDsl
class QueryBuilder(private val text: String) {
    private var type: ApiType? = null
    private var limit: Int = 10
    private var options: SearchOptions = SearchOptions()

    /**
     * Установка типа API элемента
     */
    fun type(apiType: ApiType) {
        this.type = apiType
    }

    /**
     * Установка лимита результатов
     */
    fun limit(count: Int) {
        this.limit = count
    }

    /**
     * Конфигурация опций поиска
     */
    fun options(block: SearchOptions.() -> Unit) {
        this.options.apply(block)
    }

    /**
     * Построение финального поискового запроса
     */
    fun build(): SearchQuery = SearchQuery(text, type, limit, options)
}

/**
 * Основная DSL функция для создания поискового запроса
 *
 * Использование:
 * ```kotlin
 * search {
 *     query("справочник") {
 *         type(ApiType.METHOD)
 *         limit(10)
 *         options {
 *             intelligent = true
 *             exactMatch = false
 *         }
 *     }
 * }
 * ```
 */
fun search(block: SearchContext.() -> SearchQuery): SearchQuery {
    return SearchContext().block()
} 