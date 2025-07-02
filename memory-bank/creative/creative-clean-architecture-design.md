# Clean Architecture Design для mcp-bsl-context

## 🎯 Архитектурное видение

### Главная идея: Hexagonal Architecture + DDD + Plugin System

Создаем модульную архитектуру, готовую к интеграции с RAG, полнотекстовым поиском и новыми источниками данных через четкое разделение на слои и абстракции.

## 🏗️ Детальные архитектурные решения

### 1. Domain Layer - Доменные сущности

#### ApiElement - базовая абстракция для всех элементов API
```kotlin
package ru.alkoleft.context.core.domain.api

/**
 * Базовая абстракция для всех элементов API платформы 1С
 * Sealed class обеспечивает type safety и exhaustive when
 */
sealed class ApiElement {
    abstract val name: String
    abstract val description: String
    abstract val source: DataSource
    
    /**
     * Уникальный идентификатор элемента для индексирования
     */
    val id: String get() = "${javaClass.simpleName.lowercase()}_${name.lowercase()}"
}

/**
 * Метод API платформы
 */
data class Method(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val signatures: List<MethodSignature>,
    val returnType: TypeReference?,
    val isGlobal: Boolean = false,
    val parentType: TypeReference? = null
) : ApiElement()

/**
 * Свойство API платформы  
 */
data class Property(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val dataType: TypeReference,
    val isReadonly: Boolean,
    val parentType: TypeReference? = null
) : ApiElement()

/**
 * Тип данных платформы
 */
data class Type(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val methods: List<Method>,
    val properties: List<Property>,
    val constructors: List<Constructor>,
    val baseTypes: List<TypeReference> = emptyList()
) : ApiElement()

/**
 * Конструктор типа
 */
data class Constructor(
    override val name: String,
    override val description: String,
    override val source: DataSource,
    val parameters: List<Parameter>,
    val parentType: TypeReference
) : ApiElement()

/**
 * Источник данных элемента API
 */
enum class DataSource {
    BSL_CONTEXT,        // Текущий источник
    CONFIGURATION,      // Будущий источник - метаданные конфигурации
    EXTENSION,          // Будущий источник - расширения
    DOCUMENTATION       // Будущий источник - документация
}
```

#### SearchQuery - модель поискового запроса
```kotlin
package ru.alkoleft.context.core.domain.search

/**
 * Доменная модель поискового запроса
 * Поддерживает различные алгоритмы поиска и источники данных
 */
data class SearchQuery(
    val text: String,
    val options: SearchOptions = SearchOptions(),
    val context: SearchContext = SearchContext()
) {
    init {
        require(text.isNotBlank()) { "Search text cannot be blank" }
    }
    
    /**
     * Ключ для кэширования с учетом всех параметров
     */
    fun cacheKey(): String = buildString {
        append(text.lowercase())
        append("_${options.hashCode()}")
        append("_${context.hashCode()}")
    }
}

/**
 * Опции поиска
 */
data class SearchOptions(
    val algorithm: SearchAlgorithm = SearchAlgorithm.INTELLIGENT,
    val elementTypes: Set<ApiElementType> = ApiElementType.values().toSet(),
    val dataSources: Set<DataSource> = setOf(DataSource.BSL_CONTEXT),
    val limit: Int = 10,
    val includeInherited: Boolean = false,
    val caseSensitive: Boolean = false,
    val exactMatch: Boolean = false
) {
    init {
        require(limit > 0) { "Limit must be positive" }
        require(limit <= 100) { "Limit cannot exceed 100" }
    }
}

/**
 * Контекст поиска для дополнительной информации
 */
data class SearchContext(
    val parentType: String? = null,
    val preferredLanguage: Language = Language.RU,
    val includeDeprecated: Boolean = false,
    val relevanceThreshold: Double = 0.1
)

/**
 * Поддерживаемые алгоритмы поиска
 */
enum class SearchAlgorithm {
    FUZZY,              // Текущий нечеткий поиск
    INTELLIGENT,        // Улучшенный интеллектуальный поиск 
    RAG,                // Будущий RAG поиск
    FULL_TEXT,          // Будущий полнотекстовый поиск
    SEMANTIC,           // Будущий семантический поиск
    HYBRID              // Комбинированный поиск
}

enum class ApiElementType {
    METHOD, PROPERTY, TYPE, CONSTRUCTOR
}

enum class Language {
    RU, EN
}
```

### 2. Ports (Hexagonal Architecture)

#### Входящие порты (Use Cases)
```kotlin
package ru.alkoleft.context.core.ports.incoming

/**
 * Входящий порт для поисковых операций
 */
interface SearchUseCase {
    suspend fun search(query: SearchQuery): SearchResult
    suspend fun getDetailedInfo(elementId: String): ApiElement?
    suspend fun getMemberInfo(typeName: String, memberName: String): ApiElement?
    suspend fun getTypeMembers(typeName: String): List<ApiElement>
}

/**
 * Входящий порт для работы с контекстом платформы
 */
interface ContextUseCase {
    suspend fun loadContext(path: String): PlatformContext
    suspend fun refreshContext(): PlatformContext
    suspend fun getContextStatus(): ContextStatus
}

/**
 * Входящий порт для работы с метаданными
 */
interface MetadataUseCase {
    suspend fun loadMetadata(source: DataSource): List<ApiElement>
    suspend fun refreshMetadata(source: DataSource): List<ApiElement>
    suspend fun getAvailableSources(): List<DataSource>
}
```

#### Исходящие порты (Repository & External Services)
```kotlin
package ru.alkoleft.context.core.ports.outgoing

/**
 * Порт для поисковых движков
 */
interface SearchEngine {
    fun supports(algorithm: SearchAlgorithm): Boolean
    suspend fun search(query: SearchQuery, elements: List<ApiElement>): SearchResult
    suspend fun rank(query: String, elements: List<ApiElement>): List<ScoredElement>
}

/**
 * Порт для репозитория API элементов
 */
interface ApiRepository {
    suspend fun findAll(): List<ApiElement>
    suspend fun findById(id: String): ApiElement?
    suspend fun findByType(type: ApiElementType): List<ApiElement>
    suspend fun findByName(name: String): List<ApiElement>
    suspend fun findByParentType(parentType: String): List<ApiElement>
    suspend fun save(elements: List<ApiElement>)
    suspend fun clear()
}

/**
 * Порт для источников данных
 */
interface DataSourceRepository {
    fun supports(source: DataSource): Boolean
    suspend fun load(source: DataSource, path: String): List<ApiElement>
    suspend fun refresh(source: DataSource): List<ApiElement>
    suspend fun getMetadata(source: DataSource): DataSourceMetadata
}
```

### 3. Application Layer - Use Cases Implementation

#### SearchApplicationService
```kotlin
package ru.alkoleft.context.application.services

@Service
class SearchApplicationService(
    private val apiRepository: ApiRepository,
    private val searchEngineRegistry: SearchEngineRegistry,
    private val rankingService: RankingService
) : SearchUseCase {

    override suspend fun search(query: SearchQuery): SearchResult {
        // 1. Получаем элементы из репозитория
        val elements = apiRepository.findAll()
            .filter { it.source in query.options.dataSources }
            .filter { it.toApiElementType() in query.options.elementTypes }

        // 2. Выбираем подходящий поисковый движок
        val searchEngine = searchEngineRegistry.getEngine(query.options.algorithm)
            ?: throw UnsupportedOperationException("Search algorithm ${query.options.algorithm} not supported")

        // 3. Выполняем поиск
        val result = searchEngine.search(query, elements)

        // 4. Применяем ранжирование
        val rankedResult = rankingService.rank(query, result)

        // 5. Ограничиваем результат
        return rankedResult.copy(
            elements = rankedResult.elements.take(query.options.limit)
        )
    }

    override suspend fun getDetailedInfo(elementId: String): ApiElement? {
        return apiRepository.findById(elementId)
    }

    // ... остальные методы
}
```

### 4. Infrastructure Layer - Concrete Implementations

#### FuzzySearchEngine (текущая реализация)
```kotlin
package ru.alkoleft.context.infrastructure.adapters.outgoing.search

@Component
class FuzzySearchEngine : SearchEngine {
    
    override fun supports(algorithm: SearchAlgorithm): Boolean = 
        algorithm in setOf(SearchAlgorithm.FUZZY, SearchAlgorithm.INTELLIGENT)

    override suspend fun search(query: SearchQuery, elements: List<ApiElement>): SearchResult {
        val normalizedQuery = query.text.lowercase().trim()
        
        val scored = elements.asSequence()
            .mapNotNull { element ->
                val score = calculateScore(normalizedQuery, element)
                if (score >= query.context.relevanceThreshold) {
                    ScoredElement(element, score)
                } else null
            }
            .sortedByDescending { it.score }
            .toList()

        return SearchResult(
            query = query,
            elements = scored,
            totalFound = scored.size,
            searchTime = System.currentTimeMillis()
        )
    }

    private fun calculateScore(query: String, element: ApiElement): Double {
        val name = element.name.lowercase()
        
        return when {
            name == query -> 1.0                    // Точное совпадение
            name.startsWith(query) -> 0.9           // Префиксное совпадение  
            name.contains(query) -> 0.7             // Частичное совпадение
            else -> calculateFuzzyScore(query, name) // Нечеткое совпадение
        }
    }
    
    private fun calculateFuzzyScore(query: String, name: String): Double {
        // Реализация нечеткого поиска (Levenshtein distance, и т.д.)
        // Возвращает 0.0-0.6 в зависимости от похожести
        return 0.0 // Заглушка
    }
}
```

#### CompositeSearchEngine для множественных алгоритмов
```kotlin
package ru.alkoleft.context.infrastructure.adapters.outgoing.search

@Component
class CompositeSearchEngine(
    private val engines: List<SearchEngine>
) : SearchEngine {

    override fun supports(algorithm: SearchAlgorithm): Boolean = 
        algorithm == SearchAlgorithm.HYBRID || 
        engines.any { it.supports(algorithm) }

    override suspend fun search(query: SearchQuery, elements: List<ApiElement>): SearchResult {
        return when (query.options.algorithm) {
            SearchAlgorithm.HYBRID -> performHybridSearch(query, elements)
            else -> {
                val engine = engines.first { it.supports(query.options.algorithm) }
                engine.search(query, elements)
            }
        }
    }

    private suspend fun performHybridSearch(query: SearchQuery, elements: List<ApiElement>): SearchResult {
        // Комбинируем результаты нескольких поисковых движков
        val results = engines
            .filter { it.supports(SearchAlgorithm.FUZZY) || it.supports(SearchAlgorithm.FULL_TEXT) }
            .map { it.search(query, elements) }
        
        // Объединяем и ре-ранжируем результаты
        val combinedElements = results.flatMap { it.elements }
            .groupBy { it.element.id }
            .map { (_, scores) ->
                val element = scores.first().element
                val avgScore = scores.map { it.score }.average()
                ScoredElement(element, avgScore)
            }
            .sortedByDescending { it.score }

        return SearchResult(
            query = query,
            elements = combinedElements,
            totalFound = combinedElements.size,
            searchTime = System.currentTimeMillis()
        )
    }
}
```

### 5. MCP Adapter Layer

#### McpSearchController - адаптер для MCP протокола
```kotlin
package ru.alkoleft.context.infrastructure.adapters.incoming.mcp

@Service  
class McpSearchController(
    private val searchUseCase: SearchUseCase,
    private val formatter: McpFormatterService
) {

    @Tool(
        name = "search",
        description = "Поиск по API платформы 1С Предприятие с поддержкой различных алгоритмов поиска"
    )
    @Cacheable("mcp-search")
    suspend fun search(
        @ToolParam(description = "Поисковый запрос") query: String,
        @ToolParam(description = "Тип элемента API") type: String? = null,
        @ToolParam(description = "Алгоритм поиска") algorithm: String? = null,
        @ToolParam(description = "Максимальное количество результатов") limit: Int? = null
    ): String {
        try {
            val searchQuery = buildSearchQuery(query, type, algorithm, limit)
            val result = searchUseCase.search(searchQuery)
            return formatter.formatSearchResult(result)
        } catch (e: Exception) {
            return formatter.formatError("Ошибка поиска: ${e.message}")
        }
    }

    private fun buildSearchQuery(
        text: String, 
        type: String?, 
        algorithm: String?, 
        limit: Int?
    ): SearchQuery {
        val elementTypes = type?.let { parseElementType(it) } ?: ApiElementType.values().toSet()
        val searchAlgorithm = algorithm?.let { parseAlgorithm(it) } ?: SearchAlgorithm.INTELLIGENT
        val searchLimit = limit?.coerceIn(1, 50) ?: 10

        return SearchQuery(
            text = text,
            options = SearchOptions(
                algorithm = searchAlgorithm,
                elementTypes = elementTypes,
                limit = searchLimit
            )
        )
    }
}
```

## 🎯 Преимущества новой архитектуры

### 1. Готовность к расширению
- **RAG поиск**: просто добавить новый `RagSearchEngine`
- **Полнотекстовый поиск**: новый `FullTextSearchEngine` 
- **Новые источники данных**: новые `DataSourceRepository` реализации

### 2. Тестируемость
- Каждый слой изолирован и тестируется независимо
- Mocks для всех внешних зависимостей
- Unit тесты для доменной логики

### 3. Maintainability  
- Четкое разделение ответственностей
- SOLID принципы на каждом уровне
- Dependency Injection через интерфейсы

### 4. Performance
- Plugin-система позволяет выбирать оптимальный алгоритм
- Кэширование на уровне приложения и инфраструктуры
- Асинхронность через Kotlin Coroutines

## 🚀 Миграционная стратегия

### Этап 1: Domain + Ports (без breaking changes)
1. Создать новые domain модели параллельно со старыми DTO
2. Создать интерфейсы портов
3. Сохранить совместимость со старым API

### Этап 2: Application Layer
1. Реализовать Use Cases
2. Создать адаптеры для текущих сервисов
3. Тестирование на существующих данных

### Этап 3: Infrastructure Migration
1. Перенести текущую логику в новые адаптеры
2. Заменить прямые вызовы на вызовы через порты
3. Добавить новые поисковые движки

### Этап 4: MCP Layer Refactoring
1. Переписать MCP контроллеры как тонкие адаптеры
2. Убрать бизнес-логику из MCP слоя
3. Обеспечить обратную совместимость API

Это создает solid foundation для будущего развития с RAG, полнотекстовым поиском и новыми источниками данных! 