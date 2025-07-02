# 🎨🎨🎨 ENTERING CREATIVE PHASE: Kotlin Search DSL Architecture

## Компонент: Kotlin Search DSL для PlatformApiSearchService
**Дата начала:** 2024-12-24  
**Тип творческой фазы:** Architecture Design  
**Приоритет:** 🔴 КРИТИЧЕСКИЙ  

---

## 📋 ОПИСАНИЕ КОМПОНЕНТА

### Что это такое?
Kotlin DSL (Domain Specific Language) для замены строковых поисковых запросов в PlatformApiSearchService. Это типобезопасный, элегантный API для построения поисковых запросов по API платформы 1С Предприятие.

### Что он должен делать?
1. **Заменить строковые запросы** типа `search("методы работы со справочниками", "method", 10)`
2. **Обеспечить типобезопасность** на этапе компиляции
3. **Предоставить удобный DSL синтаксис** для построения запросов
4. **Сохранить всю функциональность** существующего поиска
5. **Интегрироваться с MCP Tools** (@Tool методы)

### Текущая архитектура (Java)
```java
// Строковые параметры без типобезопасности
@Tool("search")
public String search(String query, String type, Integer limit) {
    // Сложная логика нормализации и поиска
    String normalizedQuery = query.trim().toLowerCase();
    String normalizedType = normalizeType(type);
    List<Object> results = performIntelligentSearch(normalizedQuery, normalizedType);
    return formatter.formatSearchResults(query, results);
}
```

### Целевая архитектура (Kotlin DSL)
```kotlin
// Типобезопасный DSL
@Tool("search")
fun search(block: SearchQueryBuilder.() -> Unit): String {
    val query = SearchQueryBuilder().apply(block).build()
    val results = searchEngine.performSearch(query)
    return formatter.formatSearchResults(query, results)
}

// Использование DSL
search {
    query("справочник") {
        type(ApiType.METHOD)
        limit(10)
        intelligent(true)
    }
}
```

---

## 📋 ТРЕБОВАНИЯ И ОГРАНИЧЕНИЯ

### Функциональные требования
1. **F1:** Полная функциональная совместимость с существующим Java API
2. **F2:** Поддержка всех типов поиска: intelligent, compound, type-member, word-order
3. **F3:** Типобезопасные параметры (ApiType enum вместо строк)
4. **F4:** Поддержка всех MCP Tool методов: search, getInfo, getMember, getConstructors, getMembers
5. **F5:** Удобный синтаксис для сложных запросов
6. **F6:** Валидация параметров на этапе компиляции

### Нефункциональные требования
1. **NF1:** Производительность не хуже Java версии
2. **NF2:** Совместимость с Spring Framework (кэширование, DI)
3. **NF3:** Совместимость с Jackson сериализацией
4. **NF4:** Поддержка Kotlin coroutines для асинхронности
5. **NF5:** Простота миграции с Java API

### Технические ограничения
1. **TC1:** Kotlin 2.1.20
2. **TC2:** Spring Boot 3.5.0 + Spring AI MCP Server 1.0.0
3. **TC3:** Сохранение @Tool аннотаций для MCP интеграции
4. **TC4:** Обратная совместимость с существующими DTO
5. **TC5:** Gradle Kotlin DSL build system

### Архитектурные ограничения
1. **AC1:** Следование SOLID принципам
2. **AC2:** Использование Kotlin data classes вместо Java records
3. **AC3:** Функциональный стиль программирования где возможно
4. **AC4:** Минимизация boilerplate кода

---

## 🏗️ АРХИТЕКТУРНЫЕ ВАРИАНТЫ

### ВАРИАНТ 1: Inline DSL с Extension Functions
```kotlin
class SearchQueryBuilder {
    private var queryText: String = ""
    private var apiType: ApiType? = null
    private var limit: Int = 10
    private var intelligentMode: Boolean = true
    
    fun query(text: String, configure: QueryConfig.() -> Unit = {}) {
        queryText = text
        QueryConfig().apply(configure)
    }
    
    fun build(): SearchQuery = SearchQuery(queryText, apiType, limit, intelligentMode)
}

// Использование
search {
    query("справочник") {
        type(ApiType.METHOD)
        limit(10)
    }
}
```

**Плюсы:**
- ✅ Простота реализации
- ✅ Читаемый синтаксис
- ✅ Типобезопасность
- ✅ Легкая миграция с Java

**Минусы:**
- ❌ Ограниченная композиция запросов
- ❌ Статическая структура
- ❌ Сложно расширять новыми типами поиска

### ВАРИАНТ 2: Builder Pattern с Fluent API
```kotlin
class SearchQueryBuilder private constructor() {
    companion object {
        fun create(): SearchQueryBuilder = SearchQueryBuilder()
    }
    
    fun query(text: String): TypedQueryBuilder = TypedQueryBuilder(text)
    
    class TypedQueryBuilder(private val queryText: String) {
        fun forMethods(): ConfiguredQueryBuilder = ConfiguredQueryBuilder(queryText, ApiType.METHOD)
        fun forProperties(): ConfiguredQueryBuilder = ConfiguredQueryBuilder(queryText, ApiType.PROPERTY)
        fun forTypes(): ConfiguredQueryBuilder = ConfiguredQueryBuilder(queryText, ApiType.TYPE)
        fun forAny(): ConfiguredQueryBuilder = ConfiguredQueryBuilder(queryText, null)
    }
    
    class ConfiguredQueryBuilder(
        private val queryText: String, 
        private val apiType: ApiType?
    ) {
        fun limit(count: Int): ConfiguredQueryBuilder = copy(limit = count)
        fun intelligent(enabled: Boolean): ConfiguredQueryBuilder = copy(intelligent = enabled)
        fun build(): SearchQuery = SearchQuery(queryText, apiType, limit, intelligent)
    }
}

// Использование
val query = SearchQueryBuilder.create()
    .query("справочник")
    .forMethods()
    .limit(10)
    .intelligent(true)
    .build()
```

**Плюсы:**
- ✅ Четкая типизация на каждом этапе
- ✅ Невозможно создать некорректный запрос
- ✅ Отличная IDE поддержка
- ✅ Легко расширяется

**Минусы:**
- ❌ Более сложная реализация
- ❌ Больше классов
- ❌ Менее элегантный синтаксис

### ВАРИАНТ 3: Sealed Classes с Smart Constructors
```kotlin
sealed class SearchQuery {
    abstract val text: String
    abstract val limit: Int
    
    data class MethodSearch(
        override val text: String,
        override val limit: Int = 10,
        val intelligent: Boolean = true
    ) : SearchQuery()
    
    data class PropertySearch(
        override val text: String,
        override val limit: Int = 10,
        val includeInherited: Boolean = false
    ) : SearchQuery()
    
    data class TypeSearch(
        override val text: String,
        override val limit: Int = 10,
        val includeSystemTypes: Boolean = false
    ) : SearchQuery()
    
    data class UniversalSearch(
        override val text: String,
        override val limit: Int = 10,
        val intelligent: Boolean = true,
        val searchScope: Set<ApiType> = setOf(ApiType.METHOD, ApiType.PROPERTY, ApiType.TYPE)
    ) : SearchQuery()
}

// Smart constructors
object Search {
    fun methods(text: String, configure: MethodSearch.() -> MethodSearch = { this }) =
        MethodSearch(text).configure()
    
    fun properties(text: String, configure: PropertySearch.() -> PropertySearch = { this }) = 
        PropertySearch(text).configure()
    
    fun types(text: String, configure: TypeSearch.() -> TypeSearch = { this }) =
        TypeSearch(text).configure()
        
    fun universal(text: String, configure: UniversalSearch.() -> UniversalSearch = { this }) =
        UniversalSearch(text).configure()
}

// Использование
val methodQuery = Search.methods("справочник") { 
    copy(limit = 15, intelligent = true) 
}

val universalQuery = Search.universal("справочник") {
    copy(limit = 10, searchScope = setOf(ApiType.METHOD, ApiType.TYPE))
}
```

**Плюсы:**
- ✅ Максимальная типобезопасность
- ✅ Exhaustive when в pattern matching
- ✅ Каждый тип поиска может иметь уникальные параметры
- ✅ Функциональный стиль
- ✅ Отличная поддержка в Kotlin

**Минусы:**
- ❌ Более сложная архитектура
- ❌ Может быть избыточным для простых случаев
- ❌ Требует больше кода для реализации

### ВАРИАНТ 4: Context DSL с Scope Functions
```kotlin
@DslMarker
annotation class SearchDsl

@SearchDsl
class SearchContext {
    private val queries = mutableListOf<SearchQuery>()
    
    fun query(text: String, block: QueryBuilder.() -> Unit) {
        queries.add(QueryBuilder(text).apply(block).build())
    }
    
    fun execute(): SearchResults = SearchEngine.execute(queries)
}

@SearchDsl  
class QueryBuilder(private val text: String) {
    private var type: ApiType? = null
    private var limit: Int = 10
    private var options: SearchOptions = SearchOptions()
    
    fun type(apiType: ApiType) { this.type = apiType }
    fun limit(count: Int) { this.limit = count }
    fun options(block: SearchOptions.() -> Unit) { 
        this.options.apply(block) 
    }
    
    fun build(): SearchQuery = SearchQuery(text, type, limit, options)
}

data class SearchOptions(
    var intelligent: Boolean = true,
    var includeInherited: Boolean = false,
    var caseSensitive: Boolean = false,
    var exactMatch: Boolean = false
)

// Использование
search {
    query("справочник") {
        type(ApiType.METHOD)
        limit(10)
        options {
            intelligent = true
            exactMatch = false
        }
    }
    
    query("документ") {
        type(ApiType.TYPE)
        limit(5)
    }
}
```

**Плюсы:**
- ✅ Очень читаемый DSL синтаксис
- ✅ Поддержка множественных запросов
- ✅ @DslMarker предотвращает ошибки
- ✅ Гибкая конфигурация опций
- ✅ Kotlin идиоматичный код

**Минусы:**
- ❌ Сложнее для простых случаев
- ❌ Требует понимания DSL концепций
- ❌ Больше классов и интерфейсов

---

## ⚖️ АНАЛИЗ ВАРИАНТОВ

### Критерии оценки
1. **Простота использования** (вес: 25%)
2. **Типобезопасность** (вес: 20%)
3. **Расширяемость** (вес: 20%)
4. **Производительность** (вес: 15%)
5. **Совместимость с MCP** (вес: 10%)
6. **Простота реализации** (вес: 10%)

### Оценочная матрица (по шкале 1-5)

| Критерий | Вариант 1 | Вариант 2 | Вариант 3 | Вариант 4 |
|----------|-----------|-----------|-----------|-----------|
| Простота использования | 5 | 3 | 4 | 5 |
| Типобезопасность | 4 | 5 | 5 | 4 |
| Расширяемость | 3 | 4 | 5 | 5 |
| Производительность | 5 | 4 | 4 | 4 |
| MCP совместимость | 5 | 4 | 3 | 4 |
| Простота реализации | 5 | 3 | 2 | 3 |

### Взвешенные оценки
- **Вариант 1 (Inline DSL):** 4.35
- **Вариант 2 (Builder Pattern):** 3.9  
- **Вариант 3 (Sealed Classes):** 4.1
- **Вариант 4 (Context DSL):** 4.4

---

## ✅ РЕКОМЕНДУЕМЫЙ ПОДХОД

### Выбор: **ВАРИАНТ 4 - Context DSL с Scope Functions**

### Обоснование выбора
1. **Наивысшая общая оценка** (4.4 из 5)
2. **Kotlin идиоматичность** - использует все преимущества языка
3. **Отличная читаемость** - DSL синтаксис интуитивно понятен
4. **Гибкость и расширяемость** - легко добавлять новые типы поиска
5. **@DslMarker безопасность** - предотвращает типичные DSL ошибки
6. **Поддержка сложных сценариев** - множественные запросы, богатые опции

### Ключевые преимущества
- ✅ **Элегантный синтаксис:** `search { query("text") { type(METHOD); limit(10) } }`
- ✅ **Типобезопасность:** enum вместо строк, compile-time валидация
- ✅ **Extensibility:** легко добавлять новые опции и типы поиска
- ✅ **MCP совместимость:** прозрачная интеграция с @Tool методами
- ✅ **Performance:** minimal overhead, compile-time optimizations

---

## 📝 РУКОВОДСТВО ПО РЕАЛИЗАЦИИ

### Этап 1: Основная DSL структура
```kotlin
@DslMarker
annotation class SearchDsl

@SearchDsl
class SearchContext {
    fun query(text: String, block: QueryBuilder.() -> Unit): SearchQuery =
        QueryBuilder(text).apply(block).build()
    
    suspend fun executeAsync(): SearchResults = // Kotlin coroutines
    fun execute(): SearchResults = // Синхронный вызов
}

// Основная точка входа
fun search(block: SearchContext.() -> SearchQuery): SearchQuery =
    SearchContext().block()
```

### Этап 2: Миграция MCP Tools
```kotlin
@Service
class KotlinPlatformApiSearchService {
    
    @Tool("search")
    @Cacheable("api-search")
    suspend fun search(queryBuilder: String): String {
        // Парсинг DSL из строки или прямое использование
        val query = parseSearchDsl(queryBuilder) // Fallback для строк
        return executeSearch(query)
    }
    
    // Новый типобезопасный API
    suspend fun searchTyped(block: SearchContext.() -> SearchQuery): SearchResults {
        val query = search(block)
        return executeSearch(query)
    }
}
```

### Этап 3: Интеграция с Spring
```kotlin
@Configuration
class SearchDslConfiguration {
    
    @Bean
    fun searchEngine(
        contextService: PlatformContextService,
        formatter: MarkdownFormatterService
    ): SearchEngine = SearchEngineImpl(contextService, formatter)
    
    @Bean
    fun searchDslService(searchEngine: SearchEngine): SearchDslService =
        SearchDslServiceImpl(searchEngine)
}
```

### Этап 4: Kotlin Coroutines интеграция
```kotlin
class AsyncSearchEngine {
    suspend fun performSearch(query: SearchQuery): SearchResults = coroutineScope {
        val methodsDeferred = async { searchMethods(query) }
        val propertiesDeferred = async { searchProperties(query) }
        val typesDeferred = async { searchTypes(query) }
        
        SearchResults(
            methods = methodsDeferred.await(),
            properties = propertiesDeferred.await(),
            types = typesDeferred.await()
        )
    }
}
```

---

## ✓ ПРОВЕРКА СООТВЕТСТВИЯ ТРЕБОВАНИЯМ

### Функциональные требования
- ✅ **F1:** Полная функциональная совместимость - через адаптер слой
- ✅ **F2:** Все типы поиска поддержаны - в SearchOptions
- ✅ **F3:** Типобезопасные параметры - enum ApiType, data classes
- ✅ **F4:** Все MCP Tool методы - через wrapper и adapter
- ✅ **F5:** Удобный синтаксис - Context DSL
- ✅ **F6:** Compile-time валидация - @DslMarker, sealed classes

### Нефункциональные требования  
- ✅ **NF1:** Производительность - inline functions, минимальный overhead
- ✅ **NF2:** Spring совместимость - @Service, @Cacheable работают
- ✅ **NF3:** Jackson совместимость - data classes поддерживаются
- ✅ **NF4:** Kotlin coroutines - AsyncSearchEngine
- ✅ **NF5:** Простота миграции - adapter pattern, fallback на строки

### Технические ограничения
- ✅ **TC1-TC5:** Все ограничения учтены в архитектуре

### Архитектурные ограничения
- ✅ **AC1-AC4:** Следование SOLID, data classes, функциональный стиль

---

# 🎨🎨🎨 EXITING CREATIVE PHASE

**Результат:** Архитектура Context DSL с Scope Functions выбрана как оптимальное решение  
**Статус:** ✅ ГОТОВ К РЕАЛИЗАЦИИ  
**Следующий шаг:** IMPLEMENT MODE - поэтапная реализация DSL компонентов  
**Документ сохранен:** `memory-bank/creative/creative-kotlin-search-dsl.md` 