# Технический контекст mcp-bsl-context

## Технологический стек
- **Язык:** Kotlin 1.9.22 + Java 17
- **Фреймворк:** Spring Boot 3.5.0
- **Сборка:** Gradle 8.x + Kotlin DSL
- **MCP Server:** Spring AI MCP Server 1.0.0
- **Сериализация:** Jackson 2.15.2 + Kotlin Module
- **Логирование:** Logback 1.5.18 + SLF4J
- **Асинхронность:** Kotlin Coroutines
- **Concurrency:** ConcurrentHashMap + Kotlin thread-safe extensions
- **Тестирование:** JUnit 5 + AssertJ + Spring Boot Test

## Принципы разработки

### SOLID принципы (ОБЯЗАТЕЛЬНО)
- **S** - Single Responsibility Principle: каждый класс отвечает за одну задач
- **O** - Open/Closed Principle: открыт для расширения, закрыт для изменения
- **L** - Liskov Substitution Principle: подклассы должны заменять базовые классы
- **I** - Interface Segregation Principle: интерфейсы должны быть специфичными
- **D** - Dependency Inversion Principle: зависимость от абстракций, не от конкретики

### Kotlin best practices (ОБЯЗАТЕЛЬНО)
- **Data classes** - для DTO объектов вместо POJO
- **Extension functions** - для дополнительной функциональности
- **Coroutines** - для асинхронных операций вместо Thread/CompletableFuture
- **Companion objects** - для статических методов и констант
- **When expressions** - для pattern matching вместо if-else chains
- **Null safety** - строгая типизация с nullable/non-nullable типами
- **Sealed classes** - для type-safe иерархий
- **Type aliases** - для упрощения сложных типов

### Тестирование (ОБЯЗАТЕЛЬНО)
- **Покрытие:** минимум 80% unit тестов для бизнес-логики
- **Интеграционные тесты:** для всех MCP tools и Spring сервисов
- **Naming convention:** `should return expected result when given valid input()`
- **AAA pattern:** Arrange, Act, Assert
- **Обязательные тесты для:**
  - Всех публичных методов сервисов
  - Всех MCP tools (`@Tool` методов)
  - Kotlin DSL функциональности
  - Thread-safe операций

### Документирование (ОБЯЗАТЕЛЬНО)
- **KDoc:** для всех публичных классов и методов
- **README.md:** актуальная документация по использованию MCP сервера
- **MCP_SERVER_USAGE.md:** подробное руководство по настройке и использованию
- **Memory Bank:** структурированная документация проекта
- **Примеры использования:** для интеграции с AI клиентами

## Архитектурные решения

### MCP-only архитектура (Pure Kotlin)
- **McpServerApplication** - Spring Boot точка входа
- **PlatformApiSearchService** - основной MCP сервис с `@Tool` методами
- **PlatformContextService** - thread-safe управление контекстом платформы
- **MarkdownFormatterService** - форматирование результатов для AI
- **PlatformContextLoader** - асинхронная загрузка данных платформы

### Kotlin Data Classes для DTO
- **Data classes** вместо Java records/POJO
- **ApiType.kt** - типы API элементов
- **SearchQuery.kt** - параметры поисковых запросов
- **PlatformTypeDefinition.kt** - определение типа платформы
- **MethodDefinition.kt** - определение метода
- **PropertyDefinition.kt** - определение свойства
- **ParameterDefinition.kt** - определение параметра
- **Signature.kt** - сигнатура метода

### MCP Tools (Spring AI Integration)
- **search(query, type, limit)** - нечеткий поиск по API
- **info(name, type)** - детальная информация об элементе
- **getMember(typeName, memberName)** - информация об элементе типа
- **getMembers(typeName)** - все элементы типа
- **getConstructors(typeName)** - конструкторы типа

### Интеллектуальный поиск с приоритизацией
- **4-уровневая система приоритизации:** точное → префиксное → частичное → fuzzy
- **Составные запросы:** "Таблица значений" → "ТаблицаЗначений"
- **Тип+элемент запросы:** поиск методов и свойств конкретного типа
- **Русскоязычные алиасы:** 13 терминов для интуитивного поиска
- **Кэширование:** Spring Cache для оптимизации производительности

### Thread-Safe Concurrency (Kotlin)
- **ConcurrentHashMap** для индексов поиска
- **Kotlin Coroutines** для асинхронных операций
- **withLock extensions** для критических секций
- **Thread-safe initialization** через lazy delegates

## Интеграция с BSL Context
- Зависимость от модуля `bsl-context` (1c-syntax)
- Парсинг файлов платформы 1С (*.hbk)
- Загрузка API информации при старте приложения

## MCP Server интеграция
- **Spring AI MCP Server** для поддержки Model Context Protocol
- **JSON-RPC 2.0** протокол для коммуникации с AI клиентами
- **STDIO transport** для интеграции с Claude Desktop/Cursor IDE
- **Автоматическая регистрация** `@Tool` методов

## Файловая структура (Kotlin)
```
src/main/kotlin/ru/alkoleft/context/platform/
├── McpServerApplication.kt     # Spring Boot точка входа
├── mcp/                        # MCP сервисы
│   ├── PlatformApiSearchService.kt
│   ├── PlatformContextService.kt
│   ├── MarkdownFormatterService.kt
│   └── PlatformContextLoader.kt
├── dto/                        # Kotlin Data Classes
│   ├── ApiType.kt
│   ├── SearchQuery.kt
│   ├── PlatformTypeDefinition.kt
│   └── ...
├── search/                     # Поисковая подсистема
│   ├── KotlinSearchService.kt
│   └── SearchDsl.kt
└── exporter/                   # Legacy экспортеры (совместимость)
    └── BaseExporterLogic.kt
```

## Конфигурация
- **application.yml** - Spring Boot конфигурация
- **logback.xml** - логирование для development
- **logback-mcp.xml** - специальная конфигурация для MCP режима
- **gradle/wrapper** - управление версиями Gradle

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