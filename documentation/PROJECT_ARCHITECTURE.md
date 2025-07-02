# Архитектура проекта mcp-bsl-context

## Обзор проекта

**mcp-bsl-context** - это MCP (Model Context Protocol) сервер для предоставления стандартизированного доступа к API платформы 1С Предприятие для AI ассистентов (Claude Desktop, Cursor IDE и других). Проект реализован с использованием современных принципов Clean Architecture и Hexagonal Architecture.

## Технологический стек

- **Язык:** Kotlin 2.1.20 + Java 17
- **Фреймворк:** Spring Boot 3.5.0
- **Архитектурный стиль:** Clean Architecture + Hexagonal Architecture
- **MCP Протокол:** Spring AI MCP Server 1.0.0
- **Сборка:** Gradle 8.x + Kotlin DSL
- **Сериализация:** Jackson 2.15.2 + Kotlin Module
- **Асинхронность:** Kotlin Coroutines
- **Логирование:** Logback 1.5.18 + SLF4J
- **Тестирование:** JUnit 5 + AssertJ + Spring Boot Test

## Архитектурная структура

### Схема Clean Architecture

```mermaid
graph TB
    subgraph INFRA["🔧 INFRASTRUCTURE LAYER"]
        subgraph INCOMING["📥 INCOMING ADAPTERS"]
            MCP["McpSearchController<br/>@Tool methods"]
        end
        
        subgraph OUTGOING["📤 OUTGOING ADAPTERS"]
            REPO[PlatformApiRepository<br/>BSL Context Integration]
            ENGINES[SearchEngines<br/>Intelligent • Fuzzy • Composite]
            FORMATTERS[ResultFormatters<br/>Markdown • JSON • Plain]
        end
    end
    
    subgraph APP["⚙️ APPLICATION LAYER"]
        subgraph USECASES["🎯 USE CASES"]
            SUC[SearchUseCaseImpl<br/>Business Logic Orchestration]
        end
        
        subgraph APPSERVICES["🔄 APPLICATION SERVICES"]
            SAS[SearchApplicationService<br/>Coordination & Formatting]
            FRS[FormatterRegistryService<br/>Strategy Management]
        end
    end
    
    subgraph CORE["💎 CORE LAYER"]
        subgraph ENTITIES["📊 DOMAIN ENTITIES"]
            API[ApiElement<br/>Method • Property • Type • Constructor]
            QUERY[SearchQuery<br/>Options & Context]
            RESULT[SearchResult<br/>Ranked Results]
        end
        
        subgraph DOMAINSERVICES["🏗️ DOMAIN SERVICES"]
            SS[SearchService<br/>Search Coordination]
            CS[ContextService<br/>Context Management]
            RS[RankingService<br/>Relevance Calculation]
        end
        
        subgraph INPORTS["📥 INCOMING PORTS"]
            ISUC[SearchUseCase<br/>Interface]
            ICUP[ContextUseCase<br/>Interface]
        end
        
        subgraph OUTPORTS["📤 OUTGOING PORTS"]
            IREPO[ApiRepository<br/>Interface]
            IENGINE[SearchEngine<br/>Interface]
            IFORMATTER[ResultFormatter<br/>Interface]
        end
    end
    
    %% Connections
    MCP --> SAS
    SAS --> SUC
    SUC --> SS
    SS --> CS
    SS --> RS
    
    %% Port implementations
    SUC -.->|implements| ISUC
    REPO -.->|implements| IREPO
    ENGINES -.->|implements| IENGINE
    FORMATTERS -.->|implements| IFORMATTER
    
    %% Dependencies (Dependency Inversion)
    SS --> IREPO
    SS --> IENGINE
    SAS --> IFORMATTER
    
    %% Styling
    classDef infraLayer fill:#e1f5fe,stroke:#0277bd,stroke-width:2px
    classDef appLayer fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px
    classDef coreLayer fill:#e8f5e8,stroke:#2e7d32,stroke-width:2px
    classDef port fill:#fff3e0,stroke:#ef6c00,stroke-width:2px,stroke-dasharray: 5 5
    
    class MCP,REPO,ENGINES,FORMATTERS infraLayer
    class SUC,SAS,FRS appLayer
    class API,QUERY,RESULT,SS,CS,RS coreLayer
    class ISUC,ICUP,IREPO,IENGINE,IFORMATTER port
```

### Файловая структура по слоям

```
src/main/kotlin/ru/alkoleft/context/
│
├── McpServerApplication.kt           # Spring Boot Entry Point
│
├── 📁 core/                         # DOMAIN LAYER (Бизнес-логика)
│   ├── 📁 domain/                   # Доменные модели
│   │   ├── api/
│   │   │   └── ApiElement.kt        # Базовая абстракция API элементов
│   │   └── search/
│   │       ├── SearchQuery.kt       # Модель поискового запроса
│   │       └── SearchResult.kt      # Модель результата поиска
│   │
│   ├── 📁 ports/                    # Интерфейсы (Порты)
│   │   ├── incoming/                # Use Cases
│   │   │   ├── SearchUseCase.kt
│   │   │   └── ContextUseCase.kt
│   │   └── outgoing/                # Репозитории и сервисы
│   │       ├── ApiRepository.kt
│   │       ├── SearchEngine.kt
│   │       └── ResultFormatter.kt
│   │
│   └── 📁 services/                 # Доменные сервисы
│       ├── SearchService.kt         # Координация поиска
│       ├── ContextService.kt        # Управление контекстом
│       └── RankingService.kt        # Ранжирование результатов
│
├── 📁 application/                  # APPLICATION LAYER (Оркестрация)
│   ├── 📁 usecases/                 # Реализации Use Cases
│   │   └── SearchUseCaseImpl.kt     # Основная бизнес-логика
│   │
│   ├── 📁 services/                 # Прикладные сервисы
│   │   ├── SearchApplicationService.kt   # Координация поиска
│   │   └── FormatterRegistryService.kt   # Управление форматтерами
│   │
│   ├── 📁 dto/                      # Data Transfer Objects
│   │   ├── SearchRequest.kt
│   │   ├── SearchResponse.kt
│   │   └── ElementInfoRequest.kt
│   │
│   └── 📁 configuration/
│       └── ApplicationConfiguration.kt
│
├── 📁 infrastructure/               # INFRASTRUCTURE LAYER (Адаптеры)
│   ├── 📁 adapters/
│   │   ├── incoming/                # Входящие адаптеры
│   │   │   └── mcp/
│   │   │       └── McpSearchController.kt  # MCP API контроллер
│   │   │
│   │   └── outgoing/                # Исходящие адаптеры
│   │       ├── repositories/
│   │       │   ├── PlatformApiRepository.kt
│   │       │   └── mappers/
│   │       │       └── DomainModelMapper.kt
│   │       │
│   │       ├── search/              # Поисковые движки
│   │       │   ├── IntelligentSearchEngine.kt
│   │       │   ├── FuzzySearchEngine.kt
│   │       │   └── CompositeSearchEngine.kt
│   │       │
│   │       └── formatters/          # Форматтеры результатов
│   │           └── MarkdownFormatter.kt
│   │
│   └── 📁 configuration/
│       └── InfrastructureConfiguration.kt
│
└── 📁 platform/                    # LEGACY INTEGRATION
    ├── 📁 dto/                     # Platform DTOs
    ├── 📁 mcp/                     # MCP Services
    ├── 📁 search/                  # Search DSL
    └── 📁 exporter/                # Platform Logic
```

## Компоненты системы

### Core Layer (Доменный слой)

#### Доменные модели (`core/domain/`)

```mermaid
classDiagram
    class ApiElement {
        <<abstract>>
        +String name
        +String description  
        +DataSource source
        +String id
    }
    
    class Method {
        +List~MethodSignature~ signatures
        +TypeReference? returnType
        +Boolean isGlobal
        +TypeReference? parentType
    }
    
    class Property {
        +TypeReference dataType
        +Boolean isReadonly
        +TypeReference? parentType
    }
    
    class Type {
        +List~Method~ methods
        +List~Property~ properties
        +List~Constructor~ constructors
        +List~TypeReference~ baseTypes
    }
    
    class Constructor {
        +List~Parameter~ parameters
        +TypeReference parentType
    }
    
    class MethodSignature {
        +List~Parameter~ parameters
        +TypeReference? returnType
        +Boolean isDeprecated
    }
    
    class Parameter {
        +String name
        +TypeReference type
        +Boolean isOptional
        +String? defaultValue
        +String description
    }
    
    class TypeReference {
        +String name
        +String fullName
        +Boolean isArray
        +Boolean isNullable
    }
    
    class SearchQuery {
        +String text
        +SearchOptions options
        +SearchContext context
    }
    
    class SearchResult {
        +List~SearchResultItem~ items
        +Int totalCount
        +Long executionTimeMs
    }
    
    class SearchResultItem {
        +ApiElement element
        +Double relevanceScore
        +MatchReason matchReason
        +String highlightedText
    }
    
    class SearchOptions {
        +SearchAlgorithm algorithm
        +Set~ApiElementType~ elementTypes
        +Set~DataSource~ dataSources
        +Int limit
        +Boolean includeInherited
        +Boolean caseSensitive
        +Boolean exactMatch
    }
    
    class SearchContext {
        +String? parentType
        +Language preferredLanguage
        +Boolean includeDeprecated
        +Double relevanceThreshold
    }
    
    %% Inheritance relationships
    ApiElement <|-- Method
    ApiElement <|-- Property
    ApiElement <|-- Type
    ApiElement <|-- Constructor
    
    %% Composition relationships
    Method *-- MethodSignature
    MethodSignature *-- Parameter
    Property *-- TypeReference
    Type *-- Method
    Type *-- Property
    Type *-- Constructor
    Constructor *-- Parameter
    Parameter *-- TypeReference
    
    SearchQuery *-- SearchOptions
    SearchQuery *-- SearchContext
    SearchResult *-- SearchResultItem
    SearchResultItem *-- ApiElement
    
    %% Enums
    class DataSource {
        <<enumeration>>
        BSL_CONTEXT
        CONFIGURATION
        EXTENSION
        DOCUMENTATION
    }
    
    class SearchAlgorithm {
        <<enumeration>>
        FUZZY
        INTELLIGENT
        RAG
        FULL_TEXT
        SEMANTIC
        HYBRID
    }
    
    class ApiElementType {
        <<enumeration>>
        METHOD
        PROPERTY
        TYPE
        CONSTRUCTOR
    }
    
    class Language {
        <<enumeration>>
        RU
        EN
    }
    
    class MatchReason {
        <<enumeration>>
        ExactMatch
        PrefixMatch
        ContainsMatch
        FuzzyMatch
        DescriptionMatch
    }
```

**Ключевые особенности доменных моделей:**

**ApiElement** - Базовая sealed class для всех элементов API платформы:
- Обеспечивает type safety через sealed class
- Уникальный ID для каждого элемента
- Поддержка различных источников данных

**SearchQuery** - Типобезопасная модель поискового запроса:
- Валидация входных данных на уровне конструктора
- Конфигурируемые алгоритмы поиска
- Поддержка контекстной информации

**SearchResult** - Модель результата с метриками производительности:
- Ранжированные результаты с релевантностью
- Информация о времени выполнения
- Детали совпадений для подсветки

#### Порты (Интерфейсы) (`core/ports/`)

**Входящие порты (incoming/):**
- `SearchUseCase` - основные поисковые операции
- `ContextUseCase` - управление контекстом поиска

**Исходящие порты (outgoing/):**
- `ApiRepository` - доступ к данным API
- `SearchEngine` - алгоритмы поиска  
- `ResultFormatter` - форматирование результатов

#### Доменные сервисы (`core/services/`)

- **SearchService** - координация поисковых операций
- **ContextService** - управление контекстом платформы
- **RankingService** - алгоритмы ранжирования результатов

### Application Layer (Прикладной слой)

#### Use Cases (`application/usecases/`)

**SearchUseCaseImpl** - Главный оркестратор поисковой логики:
```kotlin
class SearchUseCaseImpl(
    private val searchService: SearchService
) : SearchUseCase {
    
    suspend fun search(query: SearchQuery): SearchResult
    suspend fun getDetailedInfo(elementId: String): ApiElement?
    suspend fun getMemberInfo(typeName: String, memberName: String): ApiElement?
    suspend fun getTypeMembers(typeName: String): List<ApiElement>
    suspend fun getConstructors(typeName: String): List<ApiElement>
}
```

#### Прикладные сервисы (`application/services/`)

- **SearchApplicationService** - координация поиска между слоями
- **FormatterRegistryService** - управление стратегиями форматирования

### Infrastructure Layer (Инфраструктурный слой)

#### Входящие адаптеры (`infrastructure/adapters/incoming/`)

**McpSearchController** - MCP API контроллер, предоставляющий 5 инструментов:

```kotlin
@Service
class McpSearchController(
    private val searchApplicationService: SearchApplicationService
) {
    
    @Tool("search")    // Поиск по API платформы
    @Tool("info")      // Детальная информация об элементе
    @Tool("getMember") // Информация о члене типа
    @Tool("getMembers")// Все члены типа
    @Tool("getConstructors") // Конструкторы типа
}
```

#### Исходящие адаптеры (`infrastructure/adapters/outgoing/`)

**Репозитории:**
- `PlatformApiRepository` - доступ к данным платформы 1С

**Поисковые движки:**
- `IntelligentSearchEngine` - интеллектуальный поиск с множественными стратегиями
- `FuzzySearchEngine` - нечеткий поиск
- `CompositeSearchEngine` - комбинированный поиск

**Форматтеры:**
- `MarkdownFormatter` - форматирование в Markdown
- Поддержка JSON, Plain Text форматтеров

## Основные Use Cases

### 1. Поиск по API платформы

```mermaid
sequenceDiagram
    participant AI as 🤖 AI Assistant<br/>(Claude/Cursor)
    participant MCP as McpSearchController<br/>@Tool("search")
    participant SAS as SearchApplicationService
    participant SUC as SearchUseCaseImpl
    participant SS as SearchService
    participant SE as SearchEngine
    participant AR as ApiRepository
    participant RS as RankingService
    participant FR as FormatterRegistry

    AI->>+MCP: search("НайтиПоСсылке", "method", 10)
    Note over AI,MCP: JSON-RPC 2.0 / STDIO
    
    MCP->>+SAS: performSearch(SearchRequest)
    SAS->>+SUC: search(SearchQuery)
    SUC->>+SS: search(SearchQuery)
    
    SS->>+AR: loadElements(DataSource.BSL_CONTEXT)
    AR-->>-SS: List<ApiElement>
    
    SS->>+SE: search(query, candidates)
    Note over SE: Intelligent Search:<br/>Exact • Prefix • Contains<br/>CamelCase • Fuzzy
    SE-->>-SS: List<SearchResultItem>
    
    SS->>+RS: rankResults(items, context)
    Note over RS: Relevance scoring<br/>BSL-specific bonuses
    RS-->>-SS: List<RankedResult>
    
    SS-->>-SUC: SearchResult
    SUC-->>-SAS: SearchResult
    
    SAS->>+FR: format(result, MARKDOWN)
    FR-->>-SAS: Formatted String
    
    SAS-->>-MCP: SearchResponse
    MCP-->>-AI: Markdown результаты
    
    Note over AI: 📋 Получает структурированные<br/>результаты с релевантностью
```

### 2. Получение детальной информации

```mermaid
sequenceDiagram
    participant AI as 🤖 AI Assistant
    participant MCP as McpSearchController<br/>@Tool("info")
    participant SAS as SearchApplicationService
    participant SUC as SearchUseCaseImpl
    participant SS as SearchService
    participant AR as ApiRepository

    AI->>+MCP: info("НайтиПоСсылке", "method")
    
    MCP->>+SAS: performSearch(exactMatch=true)
    SAS->>+SUC: getDetailedInfo("НайтиПоСсылке")
    SUC->>+SS: findByExactName("НайтиПоСсылке")
    
    SS->>+AR: findByName("НайтиПоСсылке", METHOD)
    Note over AR: Поиск в кэшированных<br/>элементах API
    AR-->>-SS: ApiElement?
    
    SS-->>-SUC: ApiElement
    SUC-->>-SAS: ApiElement
    
    SAS->>SAS: format(element, DETAILED)
    SAS-->>-MCP: Detailed Info
    MCP-->>-AI: 📖 Полная информация<br/>с сигнатурами
```

### 3. Исследование типа данных

```mermaid
sequenceDiagram
    participant AI as 🤖 AI Assistant
    participant MCP as McpSearchController<br/>@Tool("getMembers")
    participant SAS as SearchApplicationService
    participant SUC as SearchUseCaseImpl
    participant SS as SearchService
    participant AR as ApiRepository

    AI->>+MCP: getMembers("СправочникСсылка")
    
    MCP->>+SAS: getTypeMembers("СправочникСсылка")
    SAS->>+SUC: getTypeMembers("СправочникСсылка")
    SUC->>+SS: findMembersByType("СправочникСсылка")
    
    SS->>+AR: findMembersByTypeName("СправочникСсылка")
    Note over AR: Поиск всех методов<br/>и свойств типа
    AR-->>-SS: List<ApiElement>
    
    SS-->>-SUC: List<ApiElement>
    SUC-->>-SAS: List<ApiElement>
    
    SAS->>SAS: formatTypeMembers(elements)
    SAS-->>-MCP: Formatted Members
    MCP-->>-AI: 📚 Все методы и свойства<br/>типа с описаниями
```

### 4. Поиск конструкторов

```mermaid
sequenceDiagram
    participant AI as 🤖 AI Assistant
    participant MCP as McpSearchController<br/>@Tool("getConstructors")
    participant SAS as SearchApplicationService
    participant SUC as SearchUseCaseImpl
    participant SS as SearchService
    participant AR as ApiRepository

    AI->>+MCP: getConstructors("Строка")
    
    MCP->>+SAS: getConstructors("Строка")
    SAS->>+SUC: getConstructors("Строка")
    SUC->>+SS: findConstructorsByType("Строка")
    
    SS->>+AR: findConstructorsByTypeName("Строка")
    Note over AR: Поиск конструкторов<br/>для типа данных
    AR-->>-SS: List<Constructor>
    
    SS-->>-SUC: List<Constructor>
    SUC-->>-SAS: List<Constructor>
    
    SAS->>SAS: formatConstructors(constructors)
    SAS-->>-MCP: Formatted Constructors
    MCP-->>-AI: 🏗️ Способы создания<br/>объектов типа
```

## Алгоритм интеллектуального поиска

### Многоуровневая стратегия поиска

```mermaid
flowchart TD
    Start(["🔍 Search Query<br/>\"НайтиПоСсылке\""]) --> LoadCandidates["📂 Load API Elements<br/>from Repository"]
    
    LoadCandidates --> ExactMatch{Exact Match<br/>query == name}
    ExactMatch -->|Yes| Score1[Score: 1.0<br/>Perfect Match]
    ExactMatch -->|No| PrefixMatch{"Prefix Match<br/>name.startsWith(query)"}
    
    PrefixMatch -->|Yes| Score2[Score: 0.9<br/>Prefix Match]
    PrefixMatch -->|No| ContainsMatch{"Contains Match<br/>name.contains(query)"}
    
    ContainsMatch -->|Yes| Score3[Score: 0.7<br/>Substring Match]
    ContainsMatch -->|No| CamelCase{"CamelCase Match<br/>extractInitials(name)"}
    
    CamelCase -->|Yes| Score4[Score: 0.8<br/>CamelCase Match]
    CamelCase -->|No| Acronym{"Acronym Match<br/>firstLetters(words)"}
    
    Acronym -->|Yes| Score5[Score: 0.75<br/>Acronym Match]
    Acronym -->|No| FuzzyMatch{Fuzzy Match<br/>levenshteinDistance}
    
    FuzzyMatch -->|similarity > 0.4| Score6[Score: 0.6<br/>Fuzzy Match]
    FuzzyMatch -->|No| DescriptionMatch{"Description Match<br/>description.contains(query)"}
    
    DescriptionMatch -->|Yes| Score7[Score: 0.4<br/>Description Match]
    DescriptionMatch -->|No| NoMatch[No Match<br/>Score: 0.0]
    
    Score1 --> Enhance[Enhance Score]
    Score2 --> Enhance
    Score3 --> Enhance
    Score4 --> Enhance
    Score5 --> Enhance
    Score6 --> Enhance
    Score7 --> Enhance
    
    Enhance --> BSLBonus{BSL Specific?}
    BSLBonus -->|Yes| AddBSLBonus[+0.1 BSL Bonus]
    BSLBonus -->|No| TypeBonus{Type Match?}
    AddBSLBonus --> TypeBonus
    
    TypeBonus -->|Yes| AddTypeBonus[+0.05 Type Bonus]
    TypeBonus -->|No| Filter{Score >= 0.1?}
    AddTypeBonus --> Filter
    
    Filter -->|Yes| Include[✅ Include in Results]
    Filter -->|No| Exclude[🚫 Filter Out]
    
    Include --> Sort[Sort by Score DESC]
    Sort --> Results([Ranked Results])
    
    NoMatch --> Exclude
    Exclude --> Sort
    
    %% Styling
    classDef matchStrategy fill:#e3f2fd,stroke:#1976d2,stroke-width:2px
    classDef scoreNode fill:#e8f5e8,stroke:#388e3c,stroke-width:2px
    classDef enhanceNode fill:#fff3e0,stroke:#f57c00,stroke-width:2px
    classDef resultNode fill:#fce4ec,stroke:#c2185b,stroke-width:2px
    
    class ExactMatch,PrefixMatch,ContainsMatch,CamelCase,Acronym,FuzzyMatch,DescriptionMatch matchStrategy
    class Score1,Score2,Score3,Score4,Score5,Score6,Score7 scoreNode
    class Enhance,BSLBonus,AddBSLBonus,TypeBonus,AddTypeBonus,Filter enhanceNode
    class Include,Sort,Results resultNode
```

### Система ранжирования

#### Базовые веса стратегий

| Стратегия | Вес | Описание |
|-----------|-----|----------|
| **Точное совпадение** | 1.0 | Полное соответствие имени запросу |
| **Префиксное совпадение** | 0.9 | Имя начинается с запроса |
| **Содержание** | 0.7 | Имя содержит запрос |
| **CamelCase совпадение** | 0.8 | Поиск по заглавным буквам |
| **Акроним** | 0.75 | Поиск по акронимам |
| **Fuzzy поиск** | 0.6 | Нечеткое соответствие (Levenshtein) |
| **Поиск в описании** | 0.4 | Поиск в текстах описаний |

#### Бонусы и пороги

- ** BSL-специфичность**: +0.1 (для элементов, характерных для 1С)
- ** Соответствие типа**: +0.05 (точное соответствие типа элемента)
- ** Минимальный порог релевантности**: 0.1
- ** Порог fuzzy similarity**: 0.4

## Конфигурация и развертывание

### Spring Boot Configuration

```yaml
# application.yml
spring:
  ai:
    mcp:
      server:
        enabled: true
        transport: stdio
        
logging:
  level:
    ru.alkoleft.context: INFO
    org.springframework.ai: DEBUG
```

### Сборка проекта

```bash
# Сборка JAR
./gradlew bootJar

# Запуск MCP сервера
java -jar build/libs/mcp-bsl-context-*.jar \
  --platform-path="/opt/1cv8/x86_64/8.3.25.1257"
```

### Docker развертывание

```dockerfile
FROM openjdk:17-jre-slim
COPY build/libs/mcp-bsl-context-*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Интеграция с AI клиентами

### Claude Desktop
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java",
      "args": ["-jar", "/path/to/mcp-bsl-context.jar", 
               "--platform-path", "/opt/1cv8/x86_64/8.3.25.1257"]
    }
  }
}
```

### Cursor IDE
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java", 
      "args": ["-jar", "/path/to/mcp-bsl-context.jar",
               "--platform-path", "/opt/1cv8/x86_64/8.3.25.1257"]
    }
  }
}
```

## Конфигурация Spring DI

### Структура конфигурации по слоям

```kotlin
// Core Configuration
@Configuration
@ComponentScan("ru.alkoleft.context.core")
class CoreConfiguration

// Application Configuration  
@Configuration
@ComponentScan("ru.alkoleft.context.application")
class ApplicationConfiguration

// Infrastructure Configuration
@Configuration
@ComponentScan("ru.alkoleft.context.infrastructure")
class InfrastructureConfiguration {
    
    @Bean
    fun compositeSearchEngine(engines: List<SearchEngine>): CompositeSearchEngine
    
    @Bean  
    fun formatterRegistry(formatters: List<ResultFormatter>): FormatterRegistryService
}
```

## Принципы взаимодействия компонентов

### Dependency Inversion Principle
- **Core слой** не зависит от Infrastructure
- **Все зависимости** через интерфейсы (порты)  
- **Infrastructure** реализует порты Core слоя

### Single Responsibility Principle
- **Каждый компонент** имеет единственную ответственность
- **Тонкие контроллеры** делегируют в Application слой
- **Application слой** оркестрирует Domain операции

### Strategy Pattern Implementation
- **SearchEngine** - различные алгоритмы поиска
- **ResultFormatter** - различные форматы вывода
- **Registry** для управления стратегиями

### Repository Pattern Implementation  
- **Абстракция** доступа к данным платформы
- **Кэширование** и оптимизация производительности
- **Thread-safe** операции

## Принципы разработки

### SOLID принципы
- **S** - Single Responsibility: каждый класс ≤ 100 LOC
- **O** - Open/Closed: расширение через стратегии без изменения кода
- **L** - Liskov Substitution: все реализации интерфейсов взаимозаменяемы
- **I** - Interface Segregation: специализированные интерфейсы
- **D** - Dependency Inversion: зависимости только от абстракций

### Design Patterns
- **Strategy Pattern** - для поисковых алгоритмов и форматтеров
- **Repository Pattern** - для доступа к данным
- **Use Case Pattern** - для бизнес-логики
- **Adapter Pattern** - для интеграции с внешними системами
- **Registry Pattern** - для управления стратегиями

### Архитектурные принципы
- **Dependency Flow**: Infrastructure → Application → Core
- **Ports & Adapters**: изоляция бизнес-логики через интерфейсы
- **Clean Architecture**: четкое разделение ответственности по слоям
- **Domain-Driven Design**: богатые доменные модели

## Тестирование

### Стратегия тестирования
- **Unit тесты**: для всех слоев архитектуры (покрытие >80%)
- **Интеграционные тесты**: для MCP tools и Spring сервисов
- **Архитектурные тесты**: валидация Clean Architecture принципов
- **E2E тесты**: полные сценарии использования MCP клиентами

### Структура тестов
```
src/test/kotlin/
├── unit/                    # Unit тесты
│   ├── core/               # Тесты доменного слоя
│   ├── application/        # Тесты прикладного слоя
│   └── infrastructure/     # Тесты инфраструктуры
├── integration/            # Интеграционные тесты
└── architecture/           # Архитектурные тесты
```

## Мониторинг и логирование

### Logback конфигурация
- Отдельная конфигурация для MCP режима
- Структурированное логирование поисковых операций
- Метрики производительности

### Spring Boot Actuator
- Health checks
- Метрики JVM
- Информация о приложении

## Планы развития

### Ближайшие улучшения
- **Полнотекстовый поиск** - семантический поиск
- **RAG поиск** - семантический поиск с векторными embeddings
- **Поддержка конфигураций** - доступ к метаданным 1С
- **Расширения** - поддержка пользовательских расширений

---

*Документация создана для проекта mcp-bsl-context v0.2.0* 