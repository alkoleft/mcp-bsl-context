# MCP Server SSE (Server-Sent Events) Режим

## Обзор

SSE (Server-Sent Events) режим позволяет запускать MCP сервер как HTTP сервер с поддержкой Server-Sent Events. Это обеспечивает:

- **Сетевой доступ** - возможность подключения к серверу по сети
- **Веб-интерфейс** - встроенный HTML клиент для тестирования
- **HTTP API** - RESTful интерфейс для интеграции с другими приложениями
- **Real-time коммуникация** - поддержка Server-Sent Events для push-уведомлений

## Запуск в SSE режиме

### Командная строка

```bash
# Базовый запуск
java -jar mcp-bsl-context.jar --sse --platform-path "/path/to/1c/platform"

# С кастомным портом
java -jar mcp-bsl-context.jar --sse --port 9000 --platform-path "/path/to/1c/platform"

# С отладочным логированием
java -jar mcp-bsl-context.jar --sse --verbose --platform-path "/path/to/1c/platform"
```

### Скрипт запуска

```bash
# Использование готового скрипта
./scripts/run-sse.sh "/path/to/1c/platform" 8080
```

### Переменные окружения

```bash
# Настройка через переменные окружения
export PLATFORM_CONTEXT_PATH="/path/to/1c/platform"
export SSE_PORT=8080
java -jar mcp-bsl-context.jar --sse
```

## Доступные эндпоинты

### 1. SSE соединение
```
GET /mcp/sse
Content-Type: text/event-stream
```

Устанавливает Server-Sent Events соединение для real-time коммуникации.

**Пример подключения:**
```javascript
const eventSource = new EventSource('/mcp/sse');

eventSource.onopen = function() {
    console.log('Подключение установлено');
};

eventSource.onmessage = function(event) {
    console.log('Получено сообщение:', event.data);
};

eventSource.addEventListener('connected', function(event) {
    const data = JSON.parse(event.data);
    console.log('Подключен к серверу:', data.server);
});
```

### 2. HTTP API для запросов
```
POST /mcp/request
Content-Type: application/json
```

Отправка MCP запросов через HTTP POST.

**Формат запроса:**
```json
{
  "id": "req-1",
  "method": "search",
  "params": {
    "query": "СтрНайти",
    "type": "method",
    "limit": 5
  }
}
```

**Формат ответа:**
```json
{
  "id": "req-1",
  "result": "Результат поиска...",
  "error": null
}
```

### 3. Проверка состояния
```
GET /mcp/health
```

Возвращает статус сервера и количество активных соединений.

**Ответ:**
```json
{
  "status": "UP",
  "server": "1C Platform API Server",
  "mode": "SSE",
  "activeConnections": 2,
  "capabilities": ["search", "info", "getMember", "getMembers", "getConstructors"]
}
```

### 4. Информация о сервере
```
GET /mcp/info
```

Возвращает информацию о сервере и доступных эндпоинтах.

**Ответ:**
```json
{
  "name": "1C Platform API Server",
  "version": "1.0.0",
  "description": "MCP сервер для поиска по API платформы 1С Предприятие",
  "transport": "SSE",
  "endpoints": {
    "sse": "/mcp/sse",
    "request": "/mcp/request",
    "health": "/mcp/health",
    "info": "/mcp/info"
  }
}
```

### 5. Веб-клиент
```
GET /sse-client.html
```

Встроенный HTML клиент для тестирования всех функций MCP сервера.

## Примеры использования

### cURL запросы

**Поиск методов:**
```bash
curl -X POST http://localhost:8080/mcp/request \
  -H "Content-Type: application/json" \
  -d '{
    "id": "search-1",
    "method": "search",
    "params": {
      "query": "СтрНайти",
      "type": "method",
      "limit": 5
    }
  }'
```

**Получение информации о методе:**
```bash
curl -X POST http://localhost:8080/mcp/request \
  -H "Content-Type: application/json" \
  -d '{
    "id": "info-1",
    "method": "info",
    "params": {
      "name": "СтрНайти",
      "type": "method"
    }
  }'
```

**Получение элементов типа:**
```bash
curl -X POST http://localhost:8080/mcp/request \
  -H "Content-Type: application/json" \
  -d '{
    "id": "member-1",
    "method": "getMember",
    "params": {
      "typeName": "СправочникСсылка",
      "memberName": "НайтиПоКоду"
    }
  }'
```

## Docker

### Запуск в Docker

```bash
# Сборка образа
docker build -t mcp-bsl-context-sse -f Dockerfile.sse .

# Запуск контейнера
docker run -d \
  -v /path/to/1c/platform:/app/1c-platform:ro \
  -p 8080:8080 \
  mcp-bsl-context-sse
```

### Docker Compose

```yaml
version: '3.8'
services:
  mcp-server:
    build:
      context: .
      dockerfile: Dockerfile.sse
    ports:
      - "8080:8080"
    volumes:
      - /path/to/1c/platform:/app/1c-platform:ro
    environment:
      - PLATFORM_CONTEXT_PATH=/app/1c-platform
```

## Мониторинг и логирование

### Логирование

В SSE режиме логи записываются в файл `mcp-server.log`:

```bash
# Просмотр логов
tail -f mcp-server.log

# Поиск ошибок
grep ERROR mcp-server.log

# Поиск SSE соединений
grep "SSE connection" mcp-server.log
```

## Устранение неполадок

### Сервер не запускается

1. **Проверьте порт:**
   ```bash
   netstat -tulpn | grep :8080
   ```

2. **Проверьте логи:**
   ```bash
   tail -f mcp-server.log
   ```

3. **Проверьте Java версию:**
   ```bash
   java -version
   ```

### SSE соединение не устанавливается

1. **Проверьте CORS настройки браузера**
2. **Убедитесь что сервер запущен в SSE режиме**
3. **Проверьте сетевые настройки**

### Медленные ответы

1. **Увеличьте heap size JVM:**
   ```bash
   java -Xmx2g -jar mcp-bsl-context.jar --sse
   ```

2. **Проверьте загрузку CPU и памяти**
3. **Оптимизируйте запросы (уменьшите лимит результатов)**

## Производительность

### Рекомендуемые настройки

```bash
# Для продакшена
java -Xms1g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -jar mcp-bsl-context.jar \
  --sse \
  --platform-path "/path/to/1c/platform"
```

### Мониторинг

```bash
# Мониторинг памяти
jstat -gc <pid> 1000

# Мониторинг потоков
jstack <pid>

# Мониторинг сети
netstat -i
``` 