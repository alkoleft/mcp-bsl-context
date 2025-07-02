# Технический контекст mcp-bsl-context

## Технологический стек
- **Язык:** Kotlin 2.1.20 + Java 17
- **Фреймворк:** Spring Boot 3.5.0
- **Сборка:** Gradle 8.x + Kotlin DSL
- **MCP Server:** Spring AI MCP Server 1.0.0
- **Сериализация:** Jackson 2.15.2 + Kotlin Module
- **Логирование:** Logback 1.5.18 + SLF4J
- **Асинхронность:** Kotlin Coroutines
- **Concurrency:** ConcurrentHashMap + Kotlin thread-safe extensions
- **Тестирование:** JUnit 5 + AssertJ + Spring Boot Test

## Архитектурные принципы

### Clean Architecture (РЕАЛИЗОВАНО)
Проект полностью переведен на Clean Architecture с четким разделением на слои:

```
📁 ru.alkoleft.context/
├── 📁 core/                     # ← DOMAIN LAYER
│   ├── domain/                  # Entities & Value Objects
│   │   ├── api/ApiElement.kt
│   │   └── search/SearchQuery.kt, SearchResult.kt
│   ├── ports/incoming/          # Use Cases Interfaces
│   │   ├── ContextUseCase.kt
│   │   └── SearchUseCase.kt
│   ├── ports/outgoing/          # Repository & Service Interfaces  
│   │   ├── ApiRepository.kt
│   │   ├── ResultFormatter.kt
│   │   └── SearchEngine.kt
│   └── services/                # Domain Services
│       ├── ContextService.kt
│       ├── RankingService.kt
│       └── SearchService.kt
│
├── 📁 application/              # ← APPLICATION LAYER
│   ├── services/                # Application Services
│   │   ├── FormatterRegistryService.kt
│   │   └── SearchApplicationService.kt
│   ├── usecases/                # Use Cases Implementation
│   │   └── SearchUseCaseImpl.kt
│   └── dto/                     # Application DTOs
│       ├── ElementInfoRequest.kt
│       ├── SearchRequest.kt
│       └── SearchResponse.kt
│
└── 📁 infrastructure/           # ← INFRASTRUCTURE LAYER
    ├── adapters/incoming/       # Controllers & Input Adapters
    │   └── mcp/McpSearchController.kt
    ├── adapters/outgoing/       # Repository & Service Implementations
    │   ├── formatters/MarkdownFormatter.kt
    │   ├── repositories/
    │   │   ├── PlatformApiRepository.kt
    │   │   └── mappers/DomainModelMapper.kt
    │   └── search/
    │       ├── CompositeSearchEngine.kt
    │       ├── FuzzySearchEngine.kt
    │       └── IntelligentSearchEngine.kt
    └── configuration/           # DI Configuration
        └── InfrastructureConfiguration.kt
```

### Hexagonal Architecture (Ports & Adapters)
- **Ports (Interfaces):** incoming/outgoing интерфейсы для изоляции бизнес-логики
- **Adapters:** конкретные реализации для интеграции с внешними системами
- **Dependency Flow:** Infrastructure → Application → Core
- **Dependency Inversion:** все зависимости через интерфейсы

### Design Patterns (ПРИМЕНЕНЫ)
- **Strategy Pattern:** ResultFormatter implementations (JsonFormatter, MarkdownFormatter)
- **Repository Pattern:** ApiRepository для доступа к данным
- **Use Case Pattern:** четкое разделение бизнес-логики
- **Adapter Pattern:** для интеграции с legacy компонентами
- **Registry Pattern:** FormatterRegistryService для управления форматтерами

## Принципы разработки

### SOLID принципы (ПОЛНОСТЬЮ РЕАЛИЗОВАНЫ)
- **S** - Single Responsibility Principle: каждый класс ≤ 100 LOC, одна ответственность
- **O** - Open/Closed Principle: стратегии для расширения без изменения кода
- **L** - Liskov Substitution Principle: все реализации интерфейсов взаимозаменяемы
- **I** - Interface Segregation Principle: специализированные интерфейсы (ContextUseCase, SearchUseCase)
- **D** - Dependency Inversion Principle: зависимости только от абстракций

### Kotlin best practices (ОБЯЗАТЕЛЬНО)
- **Data classes** - для DTO и domain entities
- **Extension functions** - для дополнительной функциональности
- **Coroutines** - для асинхронных операций в use cases
- **Companion objects** - для статических методов и констант
- **When expressions** - для pattern matching в мапперах
- **Null safety** - строгая типизация с nullable/non-nullable типами
- **Sealed classes** - для type-safe иерархий результатов
- **Type aliases** - для упрощения сложных типов

### Тестирование (ОБЯЗАТЕЛЬНО)
- **Покрытие:** минимум 80% unit тестов для всех слоев
- **Архитектурные тесты:** валидация Clean Architecture принципов
- **Интеграционные тесты:** для всех MCP tools и Spring сервисов
- **Naming convention:** `should return expected result when given valid input()`
- **AAA pattern:** Arrange, Act, Assert
- **Обязательные тесты для:**
  - Всех Use Cases (core/application слои)
  - Всех публичных методов сервисов
  - Всех MCP tools (`@Tool` методов)
  - Адаптеров и репозиториев
  - Thread-safe операций

### Документирование (ОБЯЗАТЕЛЬНО)
- **KDoc:** для всех публичных классов и методов
- **README.md:** актуальная документация по использованию MCP сервера
- **MCP_SERVER_USAGE.md:** подробное руководство по настройке и использованию
- **Memory Bank:** структурированная документация проекта
- **Архитектурные диаграммы:** для понимания Clean Architecture

## Архитектурные решения

### Clean Architecture Implementation
**Domain Layer (Core):**
- **Entities:** ApiElement, SearchQuery, SearchResult
- **Value Objects:** для immutable данных
- **Domain Services:** ContextService, RankingService, SearchService
- **Ports:** интерфейсы для изоляции от внешних зависимостей

**Application Layer:**
- **Use Cases:** SearchUseCaseImpl для бизнес-логики
- **Application Services:** SearchApplicationService, FormatterRegistryService
- **DTOs:** для передачи данных между слоями

**Infrastructure Layer:**
- **Incoming Adapters:** McpSearchController для MCP протокола
- **Outgoing Adapters:** PlatformApiRepository, search engines, formatters
- **Configuration:** Spring DI конфигурация по слоям

### MCP Integration через Clean Architecture
- **MCP Controllers** как incoming adapters
- **Use Cases** обрабатывают MCP запросы
- **Repository Pattern** для доступа к платформенным данным
- **Strategy Pattern** для форматирования результатов

### Kotlin Data Classes для Domain
- **ApiElement** - core domain entity
- **SearchQuery** - value object для поисковых запросов
- **SearchResult** - domain entity с результатами поиска
- **Все DTOs** реализованы как data classes

### Интеллектуальный поиск через Clean Architecture
- **SearchEngine interface** в core/ports/outgoing
- **Multiple implementations:** FuzzySearchEngine, IntelligentSearchEngine
- **CompositeSearchEngine** для объединения стратегий
- **RankingService** для приоритизации результатов
- **4-уровневая система:** точное → префиксное → частичное → fuzzy

### Thread-Safe Concurrency (Kotlin)
- **ConcurrentHashMap** в repositories
- **Kotlin Coroutines** в use cases
- **Immutable domain entities** для thread safety
- **Thread-safe initialization** через lazy delegates

## Интеграция с BSL Context
- **Adapter Pattern** для интеграции с legacy bsl-context модулем
- **Repository Pattern** для абстракции доступа к данным платформы
- **Mapper Pattern** для преобразования между domain и platform моделями
- Парсинг файлов платформы 1С (*.hbk) через адаптеры

## MCP Server интеграция
- **Spring AI MCP Server** для поддержки Model Context Protocol
- **JSON-RPC 2.0** протокол для коммуникации с AI клиентами
- **STDIO transport** для интеграции с Claude Desktop/Cursor IDE
- **Автоматическая регистрация** `@Tool` методов в incoming adapters

## Файловая структура (Clean Architecture)
```
src/main/kotlin/ru/alkoleft/context/
├── McpServerApplication.kt           # Spring Boot точка входа
├── core/                            # DOMAIN LAYER
│   ├── domain/                      # Entities & Value Objects
│   ├── ports/                       # Interfaces (Use Cases & Repositories)
│   └── services/                    # Domain Services
├── application/                     # APPLICATION LAYER
│   ├── services/                    # Application Services
│   ├── usecases/                    # Use Cases Implementation
│   ├── dto/                         # Application DTOs
│   └── configuration/               # Application Configuration
├── infrastructure/                  # INFRASTRUCTURE LAYER
│   ├── adapters/                    # Incoming/Outgoing Adapters
│   └── configuration/               # Infrastructure Configuration
└── platform/                       # LEGACY INTEGRATION
    ├── dto/                         # Platform DTOs
    ├── mcp/                         # MCP Services (Legacy)
    └── search/                      # Search DSL
```

## Конфигурация
- **application.yml** - Spring Boot конфигурация
- **logback.xml** - логирование для development
- **logback-mcp.xml** - специальная конфигурация для MCP режима
- **Configuration Classes** по слоям: Core, Application, Infrastructure

## Система сборки (Kotlin DSL)
- **Gradle** с Kotlin DSL вместо Groovy
- **Git версионирование** через qoomon plugin
- **Spring Boot fat JAR** через bootJar task
- **GitHub Packages** для публикации артефактов
- **Kotlin compiler options** с JSR-305 strict mode

## Интеграция с AI клиентами
- **Claude Desktop** - через claude_desktop_config.json
- **Cursor IDE** - через .cursor/mcp.json
- **MCP протокол** - стандартизированный доступ к API
- **Markdown форматирование** - оптимизировано для AI понимания

## Development процесс
- **Memory Bank workflow** - структурированная разработка
- **Level-based complexity** - от Level 1 до Level 4 задач
- **Mode transitions** - VAN → PLAN → CREATIVE → IMPLEMENT → QA → REFLECT → ARCHIVE
- **Continuous documentation** - актуализация документации на каждом этапе
- **Clean Architecture compliance** - валидация архитектурных принципов

## Архитектурные метрики
- **Total Classes:** 25+ (optimal distribution по слоям)
- **Average Class Size:** 50-100 LOC
- **Dependency Direction:** правильный (Infrastructure → Application → Core)
- **Interface Segregation:** специализированные интерфейсы
- **Cohesion:** High (каждый класс одна ответственность)
- **Coupling:** Low (зависимости только через интерфейсы) 