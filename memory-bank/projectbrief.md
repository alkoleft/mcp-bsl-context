# mcp-bsl-context - Проект краткое описание

## Обзор проекта
**mcp-bsl-context** - MCP (Model Context Protocol) сервер для предоставления стандартизированного доступа к API платформы 1С Предприятие для AI ассистентов. Проект реализован с использованием современных принципов Clean Architecture и Hexagonal Architecture.

## Цель проекта
Предоставление интерактивного MCP сервера для интеграции с AI ассистентами (Claude Desktop, Cursor IDE), включающего:
- Поиск по глобальным методам платформы 1С
- Поиск по глобальным свойствам платформы 1С
- Получение информации о типах данных с их методами и свойствами
- Получение конструкторов типов данных
- Нечеткий поиск с ранжированием результатов
- Интеллектуальная обработка запросов через Clean Architecture

## Архитектурный подход
Проект полностью реализован на основе **Clean Architecture** с четким разделением на слои:

### Domain Layer (Core)
- **Entities:** ApiElement, SearchQuery, SearchResult - основные доменные объекты
- **Value Objects:** immutable объекты для передачи данных
- **Domain Services:** ContextService, RankingService, SearchService
- **Ports:** интерфейсы для изоляции бизнес-логики от внешних зависимостей

### Application Layer
- **Use Cases:** SearchUseCaseImpl - инкапсулирует бизнес-логику
- **Application Services:** SearchApplicationService, FormatterRegistryService
- **DTOs:** ElementInfoRequest, SearchRequest, SearchResponse

### Infrastructure Layer
- **Incoming Adapters:** McpSearchController для обработки MCP запросов
- **Outgoing Adapters:** PlatformApiRepository, поисковые движки, форматтеры
- **Configuration:** Spring DI конфигурация

## Текущая функциональность
- **Clean Architecture реализация** с Hexagonal Architecture (Ports & Adapters)
- **MCP сервер** на основе Spring AI MCP Server с 5 инструментами
- **Интеллектуальный поиск** с 4-уровневой системой приоритизации
- **Strategy Pattern** для форматирования результатов (Markdown, JSON, Plain Text)
- **Repository Pattern** для абстракции доступа к данным платформы
- **Thread-safe операции** с использованием Kotlin Coroutines
- **Comprehensive testing** включая архитектурные тесты

## MCP Tools (через Clean Architecture)
- **search(query, type, limit)** - интеллектуальный поиск через SearchUseCase
- **info(name, type)** - детальная информация через ContextUseCase
- **getMember(typeName, memberName)** - информация об элементе типа
- **getMembers(typeName)** - все элементы типа
- **getConstructors(typeName)** - конструкторы типа

## Архитектура
- **Платформа:** Java 17+ + Spring Boot 3.5.0 + Kotlin
- **Архитектурный стиль:** Clean Architecture + Hexagonal Architecture
- **Design Patterns:** Strategy, Repository, Use Case, Adapter, Registry
- **Сборка:** Gradle + Kotlin DSL
- **MCP протокол:** Spring AI MCP Server 1.0.0
- **Сериализация:** Jackson (JSON)
- **Логирование:** Logback + SLF4J
- **Асинхронность:** Kotlin Coroutines
- **Dependency Injection:** Spring Boot с конфигурацией по слоям

## Основные компоненты (Clean Architecture)

### Core Layer
- **Domain Entities:** ApiElement, SearchQuery, SearchResult
- **Domain Services:** ContextService, RankingService, SearchService
- **Ports/Incoming:** ContextUseCase, SearchUseCase
- **Ports/Outgoing:** ApiRepository, SearchEngine, ResultFormatter

### Application Layer
- **Use Cases:** SearchUseCaseImpl - основная бизнес-логика
- **Application Services:** SearchApplicationService, FormatterRegistryService
- **DTOs:** для передачи данных между слоями

### Infrastructure Layer
- **Incoming Adapters:** McpSearchController - обработка MCP запросов
- **Outgoing Adapters:** 
  - PlatformApiRepository - доступ к данным платформы
  - IntelligentSearchEngine, FuzzySearchEngine - поисковые движки
  - MarkdownFormatter - форматирование результатов
- **Configuration:** по слоям для оптимального DI

### Legacy Integration
- **Platform layer** - интеграция с существующими компонентами
- **PlatformContextLoader** - загрузчик и парсер файлов контекста 1С
- **Search DSL** - специализированный поисковый язык

## Архитектурные принципы
- **SOLID принципы** - полностью реализованы во всех компонентах
- **Dependency Inversion** - зависимости только от абстракций
- **Single Responsibility** - каждый класс ≤ 100 LOC
- **Interface Segregation** - специализированные интерфейсы
- **Strategy Pattern** - для расширяемости без изменения кода

## Качество кода
- **25+ классов** оптимального размера
- **100% компиляция** без ошибок и предупреждений
- **Архитектурные тесты** - валидация Clean Architecture принципов
- **Unit тесты** - покрытие всех слоев архитектуры
- **ktlint compliance** - соответствие стандартам кода Kotlin

## Development Workflow
- **Memory Bank процесс** - структурированная разработка
- **Mode-based approach** - VAN → PLAN → CREATIVE → IMPLEMENT → QA → REFLECT → ARCHIVE
- **Clean Architecture compliance** - постоянная валидация архитектурных принципов
- **Level-based complexity** - от простых исправлений до сложных архитектурных задач 