# MCP Server SSE (Server-Sent Events) Режим

> 📖 **См. также:** [Основная документация](../README.md) | [Интеграция с IDE](05_INTEGRATION.md) | [Установка и запуск](02_SETUP.md)

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
java -jar mcp-bsl-context.jar --mode sse --platform-path "/path/to/1c/platform"

# С кастомным портом
java -jar mcp-bsl-context.jar --mode sse --port 9000 --platform-path "/path/to/1c/platform"

# С отладочным логированием
java -jar mcp-bsl-context.jar --mode sse --verbose --platform-path "/path/to/1c/platform"
```

### Переменные окружения

```bash
# Настройка через переменные окружения
export PLATFORM_CONTEXT_PATH="/path/to/1c/platform"
export SSE_PORT=8080
java -jar mcp-bsl-context.jar --mode sse
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
   java -Xmx2g -jar mcp-bsl-context.jar --mode sse
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
  --mode sse \
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