# MCP сервер для API платформы 1С Предприятие

## Описание

MCP (Model Context Protocol) сервер предоставляет стандартизированный доступ к API платформы 1С Предприятие для AI ассистентов. Сервер поддерживает поиск по глобальным методам, свойствам и типам данных платформы через 5 специализированных инструментов.

## Возможности

### Tool: search
- **Назначение** - поиск по API платформы 1С Предприятие
- **Параметры**:
  - `query` (обязательный) - поисковый запрос
  - `type` (опциональный) - тип элемента (`method`, `property`, `type`)
  - `limit` (опциональный) - максимальное количество результатов (1-50, по умолчанию 10)

### Tool: info  
- **Назначение** - получение детальной информации об элементе API
- **Параметры**:
  - `name` (обязательный) - имя элемента API
  - `type` (опциональный) - тип элемента (`method`, `property`, `type`)

### Tool: getMember
- **Назначение** - получение информации о методе или свойстве конкретного типа
- **Параметры**:
  - `typeName` (обязательный) - имя типа 1С (например, "СправочникСсылка", "ДокументОбъект")
  - `memberName` (обязательный) - имя метода или свойства типа

### Tool: getMembers
- **Назначение** - получение полного списка всех методов и свойств для указанного типа
- **Параметры**:
  - `typeName` (обязательный) - имя типа 1С для получения его элементов

### Tool: getConstructors
- **Назначение** - получение списка конструкторов для указанного типа
- **Параметры**:
  - `typeName` (обязательный) - имя типа данных 1С (например, "Массив", "Структура", "ТаблицаЗначений")

## Алгоритм поиска

Поиск выполняется по принципу нечеткого соответствия с ранжированием:

1. **Точное совпадение** (100 баллов) - имя полностью совпадает с запросом
2. **Префиксное совпадение** (80 баллов) - имя начинается с запроса  
3. **Частичное совпадение** (60 баллов) - имя содержит запрос
4. **Совпадение в сигнатуре** (40 баллов) - запрос найден в сигнатуре
5. **Совпадение в описании** (20 баллов) - запрос найден в описании
6. **Fuzzy matching** (0-50 баллов) - на основе расстояния Левенштейна

## Установка и запуск

### Требования
- Java 17+
- Платформа 1С Предприятие (для загрузки данных API)

### Сборка проекта
```bash
./gradlew build
```

### Запуск MCP сервера

```bash
java -jar build/libs/mcp-bsl-context.jar --platform-path /path/to/1c/platform
```

### Параметры запуска
- `--platform-path` (обязательный) - путь к каталогу установки 1С Предприятия
- `--verbose` - включить отладочное логирование
- `--help` - показать справку

### Пример запуска с отладкой
```bash
java -jar build/libs/mcp-bsl-context.jar \
  --platform-path "/opt/1cv8/x86_64/8.3.25.1257" \
  --verbose
```

### Запуск в Docker c STDIO
- В папке с `Dockerfile'ом` рядом должен быть JAR-файл.
- Собираем docker image:
```bash
docker build -t 1c-platform-mcp-server-stdio -f Dockerfile.stdio --build-arg APP_VERSION=х.х.х .
```

**Пример настройки IDE при запуске в Docker через STDIO**:
```json
{
    "mcpServers": {
        "1c-platform-stdio": {
            "command": "docker",
            "args": [
                "run",
                "--rm",
                "-i",
                "-v",
                "...platform/bin:/app/1c-platform:ro",
                "1c-platform-mcp-server-stdio",
                "--platform-path",
                "/app/1c-platform"
            ]
        }
    }
}
```

### Запуск в Docker c проксированием в SSE
Это вариант, если хотите запускать MCP на удаленном хосте с доступом по сети.
- Для проксирования используется пакет [Supergateway](https://github.com/supercorp-ai/supergateway)
- В папке с `Dockerfile'ом` рядом должен быть JAR-файл.
- Собираем и запускаем docker:
```bash
docker build -t 1c-platform-mcp-server-sse -f Dockerfile.sse --build-arg APP_VERSION=0.1.4 .

docker run -d \
  -v ./platform:/app/1c-platform:ro \
  -p 8001:8000 \
  1c-platform-mcp-server-sse
```

**Пример настройки IDE при запуске в Docker с SSE**:
```json
{
    "mcpServers": {
        "1c-platform-sse": {
            "url": "http://10.0.0.x:8001/sse"
        }
}
```
Актуальную версию (APP_VERSION) смотрите в [релизах](https://github.com/alkoleft/mcp-bsl-context/releases)

### Быстрый старт с опубликованными Docker-образами

Сервер MCP теперь доступен как готовый Docker-образ на GitHub Container Registry: [ghcr.io/alkoleft/mcp-bsl-context](https://github.com/alkoleft/mcp-bsl-context/pkgs/container/mcp-bsl-context)

#### Получение образа

```bash
docker pull ghcr.io/alkoleft/mcp-bsl-context:v0.2.1-sse
# или для stdio:
docker pull ghcr.io/alkoleft/mcp-bsl-context:v0.2.1-stdio
```

#### Запуск STDIO-версии

```bash
docker run --rm -i \
  -v /path/to/1c/platform:/app/1c-platform:ro \
  ghcr.io/alkoleft/mcp-bsl-context:v0.2.1-stdio \
  --platform-path /app/1c-platform
```

#### Запуск SSE-версии (с сетевым доступом)

```bash
docker run -d \
  -v /path/to/1c/platform:/app/1c-platform:ro \
  -p 8001:8000 \
  ghcr.io/alkoleft/mcp-bsl-context:v0.2.1-sse \
  --platform-path /app/1c-platform
```

- Замените `/path/to/1c/platform` на путь к вашей установленной платформе 1С (только для чтения).
- Для актуальных тегов и digest-версий смотрите [страницу пакета](https://github.com/alkoleft/mcp-bsl-context/pkgs/container/mcp-bsl-context).

#### Примеры интеграции с IDE

##### STDIO (локальный запуск через docker)
```json
{
  "mcpServers": {
    "1c-platform-stdio": {
      "command": "docker",
      "args": [
        "run", "--rm", "-i",
        "-v", "/opt/1cv8/x86_64/8.3.25.1257:/app/1c-platform:ro",
        "ghcr.io/alkoleft/mcp-bsl-context:v0.2.1-stdio",
        "--platform-path", "/app/1c-platform"
      ]
    }
  }
}
```

##### SSE (сетевой режим)
```json
{
  "mcpServers": {
    "1c-platform-sse": {
      "url": "http://localhost:8001/sse"
    }
  }
}
```

---

## Интеграция с AI клиентами

### Claude Desktop
Добавьте конфигурацию в `claude_desktop_config.json`:

Linux/Mac
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java",
      "args": [
        "-jar", 
        "/path/to/mcp-bsl-context.jar", 
        "--platform-path", 
        "/opt/1cv8/x86_64/8.3.25.1257"
      ]
    }
  }
}
```
для Windows - важно указать каталог bin платформы без указания пути с пробелами
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java",
      "args": [
        "-jar", 
        "C:\\ваш\\путь\\mcp-bsl-context.jar", 
        "--platform-path", 
        "C:\\Progra~1\\1cv8\\8.3.23.1912\\bin"
      ]
    }
  }
}
```

### Cursor IDE

Cursor поддерживает MCP серверы через конфигурационные файлы. Для подключения MCP сервера платформы 1С:

### Настройка MCP сервера
Создайте файл `.cursor/mcp.json` в корне проекта (для использования только в текущем проекте) или откройте настройки Cursor (Settings → MCP Servers) для использования во всех проектах:

Linux/Mac
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java",
      "args": [
        "-jar", 
        "/path/to/mcp-bsl-context.jar", 
        "--platform-path", 
        "/opt/1cv8/x86_64/8.3.25.1257"
      ]
    }
  }
}
```
для Windows - важно указать каталог bin платформы без указания пути с пробелами
```json
{
  "mcpServers": {
    "1c-platform": {
      "command": "java",
      "args": [
        "-jar", 
        "C:\\ваш\\путь\\mcp-bsl-context.jar", 
        "--platform-path", 
        "C:\\Progra~1\\1cv8\\8.3.23.1912\\bin"
      ]
    }
  }
}
```

### Настройка правил для работы с 1С API (рекомендуется)

**Через интерфейс Cursor (Settings → Rules):**
1. Откройте настройки Cursor (⌘,)
2. Перейдите в раздел "Rules"
3. Нажмите "+" для создания нового правила
4. Заполните поля:
   - **Название**: `1С Platform Development`
   - **Описание**: `Правила для разработки на платформе 1С с использованием MCP API`
   - **Glob pattern**: `**/*.bsl` (для BSL файлов)
   - **Правило**:
   ```
   - Всегда используй MCP инструменты для поиска методов и типов платформы 1С
   - При написании BSL кода проверяй существующие методы через mcp_1c-platform_search
   - Для сложных типов получай полную информацию через mcp_1c-platform_info
   - Используй актуальные сигнатуры методов из MCP сервера
   ```

**Через файловую систему (.cursor/rules):**
Создайте файл `.cursor/rules/1c-platform.md`:

```markdown
---
title: 1С Platform Development
description: Правила для разработки на платформе 1С с использованием MCP API
globs: ["**/*.bsl"]
---

# Правила разработки на платформе 1С

- Всегда используй MCP инструменты для поиска методов и типов платформы 1С
- При написании BSL кода проверяй существующие методы через mcp_1c-platform_search
- Для сложных типов получай полную информацию через mcp_1c-platform_info
- Используй актуальные сигнатуры методов из MCP сервера
- Предпочитай использовать стандартные методы платформы вместо самописных решений
```

### Проверка подключения
1. Перезапустите Cursor после создания конфигурации
2. Откройте настройки Cursor (Settings → MCP Servers)
3. Убедитесь что сервер `1c-platform-api` отображается как подключенный
4. Проверьте список доступных инструментов:
   - `mcp_1c-platform_search` - поиск по API платформы 1С
   - `mcp_1c-platform_info` - детальная информация об элементах API
   - `mcp_1c-platform_getMember` - информация о конкретном методе или свойстве типа
   - `mcp_1c-platform_getMembers` - полный список всех методов и свойств типа
   - `mcp_1c-platform_getConstructors` - список конструкторов типа данных

### Использование в Cursor Tab
Cursor автоматически использует MCP инструменты при необходимости. Для явного использования можно:
- Задать вопрос в чате: "Найди методы для работы с файлами в 1С"
- Попросить: "Покажи сигнатуру метода НайтиФайлы"
- Команда: "Список всех доступных инструментов"
- Запросить: "Покажи все методы типа СправочникСсылка"
- Узнать: "Как создать новый массив в 1С?" (использует getConstructors)
- Получить детали: "Что делает метод Записать у ДокументОбъект?"

### Другие MCP клиенты
Сервер поддерживает стандартный MCP протокол через STDIO транспорт, поэтому совместим с любыми MCP клиентами.

## Примеры использования

### Поиск методов для работы с файлами
```json
{
  "method": "tools/call",
  "params": {
    "name": "search",
    "arguments": {
      "query": "файл",
      "type": "method",
      "limit": 5
    }
  }
}
```

### Получение информации о конкретном методе
```json
{
  "method": "tools/call", 
  "params": {
    "name": "info",
    "arguments": {
      "name": "НайтиФайлы",
      "type": "method"
    }
  }
}
```

### Получение информации об элементе типа
```json
{
  "method": "tools/call",
  "params": {
    "name": "getMember",
    "arguments": {
      "typeName": "СправочникСсылка",
      "memberName": "НайтиПоКоду"
    }
  }
}
```

### Получение всех элементов типа
```json
{
  "method": "tools/call",
  "params": {
    "name": "getMembers",
    "arguments": {
      "typeName": "ТаблицаЗначений"
    }
  }
}
```

### Получение конструкторов типа
```json
{
  "method": "tools/call",
  "params": {
    "name": "getConstructors",
    "arguments": {
      "typeName": "Массив"
    }
  }
}
```

## Формат ответов

Сервер возвращает результаты в формате Markdown с адаптивным форматированием:

- **1 результат** - детальное описание с сигнатурой и параметрами
- **2-5 результатов** - компактное описание каждого с разделителями  
- **>5 результатов** - табличное представление топ-5 + детали первого

### Пример ответа
```markdown
# Результаты поиска: "найти файл" (3 найдено)

## НайтиФайлы
```bsl
НайтиФайлы(Путь: Строка, Маска: Строка): Массив
```
*Глобальный метод* • **Методы**

Находит файлы в указанном каталоге по маске поиска.
```

## Логирование

- **Рабочие логи** - `mcp-server.log` (автоматическая ротация)
- **Отладочные сообщения** - stderr (не мешает MCP протоколу)
- **JSON-RPC коммуникация** - через stdout/stdin

## Архитектура

Сервер построен на Spring Boot с использованием Spring AI MCP Server Boot Starter:

- **McpServerCommand** - CLI команда для запуска
- **McpServerApplication** - Spring Boot приложение
- **PlatformApiSearchService** - бизнес-логика поиска
- **MarkdownFormatterService** - форматирование результатов  
- **McpToolsConfiguration** - регистрация MCP tools

## Устранение неполадок

### Сервер не запускается
1. Проверьте корректность пути к платформе 1С
2. Убедитесь что Java 17+ установлена
3. Проверьте логи в `mcp-server.log`

### MCP клиент не видит tools
1. Убедитесь что сервер полностью загрузился
2. Проверьте совместимость версий MCP протокола
3. Включите отладочное логирование (`--verbose`)

### Медленный поиск
1. Поиск индексируется при запуске - дождитесь завершения
2. При большом объеме данных увеличьте heap size JVM
3. Результаты кэшируются автоматически

## Техническая поддержка

- **Версия MCP протокола** - 2024-11-05
- **Версия Spring AI** - 1.0.0
- **Минимальная версия Java** - 17
- **Поддерживаемые ОС** - Linux, macOS, Windows

Для сообщения об ошибках создайте issue в репозитории проекта. 
