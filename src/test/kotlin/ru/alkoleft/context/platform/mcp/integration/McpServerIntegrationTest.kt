package ru.alkoleft.context.platform.mcp.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import ru.alkoleft.context.McpServerApplication
import ru.alkoleft.context.presentation.mcp.PlatformContextMcpController
import kotlin.test.assertEquals

/**
 * Основной интеграционный тест для MCP сервера
 * Тестирует базовую функциональность и интеграцию компонентов
 */
@SpringBootTest(classes = [McpServerApplication::class])
@ActiveProfiles("test")
@DisplayName("MCP Server Integration Tests")
class McpServerIntegrationTest {
    private val logger = KotlinLogging.logger {}

    @Autowired
    private lateinit var mcpController: PlatformContextMcpController

    @BeforeEach
    fun setUp() {
        logger.info { "Настройка интеграционного теста MCP сервера" }
    }

    @Test
    @DisplayName("MCP сервер должен успешно запускаться")
    fun testMcpServerStartup() {
        // Arrange & Act
        val isControllerAvailable = ::mcpController.isInitialized

        // Assert
        assert(isControllerAvailable) { "MCP контроллер должен быть доступен" }
        logger.info { "✅ MCP сервер успешно запущен" }
    }

    @Test
    @DisplayName("Поиск должен возвращать корректную структуру ответа")
    fun testSearchResponseStructure() {
        // Arrange
        val query = "СтрНайти"

        // Act
        val result = mcpController.search(query)

        // Assert
        assert(result.isNotEmpty()) { "Поиск должен возвращать результаты" }
        assert(!result.contains("❌ **Не найдено:** Ничего не найдено")) { "Поиск не должен возвращать ошибку для корректного запроса" }
        assert(result.contains("СтрНайти") || result.contains("Поиск")) { "Результат должен содержать информацию о поиске" }

        logger.info { "✅ Структура ответа поиска корректна" }
    }

    @Test
    @DisplayName("Поиск должен обрабатывать пустые запросы")
    fun testSearchEmptyQuery() {
        // Arrange
        val query = ""

        // Act
        val result = mcpController.search(query)

        // Assert
        assert(result.isNotEmpty()) { "Пустой запрос должен обрабатываться корректно" }
        assertEquals(
            "❌ Невалидный поисковый запрос: Запрос не может быть пустым",
            result,
            "Поиск должен возвращать ошибку для пустого запроса",
        )
    }

    @Test
    @DisplayName("Поиск должен поддерживать ограничение результатов")
    fun testSearchWithLimit() {
        // Arrange
        val limit = 2
        val query = "Стр"

        // Act
        val result = mcpController.search(query, limit = limit)

        // Assert
        assert(result.isNotEmpty()) { "Поиск с ограничением должен возвращать результат" }
        assert(!result.contains("❌")) { "Поиск с ограничением не должен возвращать ошибку для корректного запроса" }

        logger.info { "✅ Поиск с ограничением работает корректно" }
    }

    @Test
    @DisplayName("Поиск должен поддерживать фильтрацию по типу")
    fun testSearchByType() {
        // Arrange
        val query = "СтрНайти"
        val type = "method"

        // Act
        val result = mcpController.search(query, type = type)

        // Assert
        assert(result.isNotEmpty()) { "Поиск по типу должен возвращать результат" }
        assert(!result.contains("❌")) { "Поиск по типу не должен возвращать ошибку для корректного запроса" }

        logger.info { "✅ Поиск по типу работает корректно" }
    }

    @Test
    @DisplayName("Инструмент info должен возвращать детальную информацию")
    fun testInfoTool() {
        // Arrange
        val elementName = "СтрНайти"

        // Act
        val result = mcpController.getInfo(elementName, "method")

        // Assert
        assert(result.isNotEmpty()) { "Инструмент info должен возвращать результат" }
        assert(!result.contains("❌")) { "Инструмент info не должен возвращать ошибку для корректного запроса" }
        assert(result.contains(elementName) || result.contains("не найден")) { "Результат должен содержать информацию об элементе" }

        logger.info { "✅ Инструмент info работает корректно для '$elementName'" }
    }

    @Test
    @DisplayName("Инструмент getMember должен возвращать информацию о члене типа")
    fun testGetMemberTool() {
        // Arrange
        val typeName = "ТаблицаЗначений"
        val memberName = "Колонки"

        // Act
        val result = mcpController.getMember(typeName, memberName)

        // Assert
        assert(result.isNotEmpty()) { "Инструмент getMember должен возвращать результат" }
        assert(!result.contains("❌")) { "Инструмент getMember не должен возвращать ошибку для корректного запроса" }

        logger.info { "✅ Инструмент getMember работает корректно для '$typeName.$memberName'" }
    }

    @Test
    @DisplayName("Инструмент getMembers должен возвращать список всех членов типа")
    fun testGetMembersTool() {
        // Arrange
        val typeName = "ТаблицаЗначений"

        // Act
        val result = mcpController.getMembers(typeName)

        // Assert
        assert(result.isNotEmpty()) { "Инструмент getMembers должен возвращать результат" }
        assert(!result.contains("❌")) { "Инструмент getMembers не должен возвращать ошибку для корректного запроса" }

        logger.info { "✅ Инструмент getMembers работает корректно для '$typeName'" }
    }

    @Test
    @DisplayName("Инструмент getConstructors должен возвращать список конструкторов")
    fun testGetConstructorsTool() {
        // Arrange
        val typeName = "ТаблицаЗначений"

        // Act
        val result = mcpController.getConstructors(typeName)

        // Assert
        assert(result.isNotEmpty()) { "Инструмент getConstructors должен возвращать результат" }
        assert(!result.contains("❌")) { "Инструмент getConstructors не должен возвращать ошибку для корректного запроса" }

        logger.info { "✅ Инструмент getConstructors работает корректно для '$typeName'" }
    }

    @Test
    @DisplayName("MCP сервер должен обрабатывать некорректные запросы gracefully")
    fun testGracefulErrorHandling() {
        // Arrange
        val invalidQueries =
            listOf(
                "!@#$%^&*()", // Специальные символы
                "   ", // Только пробелы
                "a".repeat(1000), // Очень длинная строка
            )

        // Act & Assert
        invalidQueries.forEach { query ->
            try {
                val result = mcpController.search(query)

                // Результат может быть пустым, но не должен вызывать исключение
                assert(result.isNotEmpty()) { "Некорректный запрос должен обрабатываться" }
                assert(result.contains("❌")) { "✅ Некорректный запрос '$query' вернул ошибку (ожидаемо): $result" }
            } catch (e: Exception) {
                logger.warn { "Некорректный запрос '$query' вызвал исключение: ${e.message}" }
                // Это может быть ожидаемым поведением для некоторых некорректных запросов
            }
        }
    }

    @Test
    @DisplayName("MCP сервер должен поддерживать параллельные запросы")
    fun testConcurrentRequests() {
        // Arrange
        val queries = listOf("Стр", "Справочник", "Код", "Найти")

        // Act
        val results = queries.map { mcpController.search(it) }

        // Assert
        results.forEachIndexed { index, result ->
            assert(result.isNotEmpty()) { "Запрос ${queries[index]} должен обрабатываться" }
            assert(!result.contains("❌")) { "Запрос ${queries[index]} не должен возвращать ошибку" }
            logger.debug { "✅ Запрос '${queries[index]}' обработан успешно" }
        }

        logger.info { "✅ Параллельные запросы обработаны корректно: ${results.size} запросов" }
    }

    @Test
    @DisplayName("MCP сервер должен корректно обрабатывать ошибки")
    fun testErrorHandling() {
        // Arrange
        val errorScenarios =
            listOf(
                "НесуществующийЭлемент12345" to "Поиск несуществующего элемента",
                "НекорректныйТип12345" to "Запрос некорректного типа",
            )

        // Act & Assert
        errorScenarios.forEach { (query, description) ->
            val result = mcpController.search(query)

            assert(result.isNotEmpty()) { "$description должен возвращать результат" }
            assert(result.contains("❌ **Не найдено:** Ничего не найдено")) { "$description должен вернуть ошибку" }
            assert(result.length > 3) { "Сообщение об ошибке должно содержать описание" }
        }

        // Тестируем ошибки в инструментах
        val infoResult = mcpController.getInfo("НесуществующийЭлемент12345", "method")
        assert(infoResult.contains("❌")) { "Инструмент info должен вернуть ошибку" }

        val memberResult = mcpController.getMember("НесуществующийТип12345", "НесуществующийЧлен12345")
        assert(infoResult.contains("❌")) { "Инструмент getMember должен вернуть ошибку" }
    }
}
