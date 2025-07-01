package ru.alkoleft.context.platform.mcp

import com.github._1c_syntax.bsl.context.api.ContextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Kotlin реализация сервиса для работы с контекстом платформы 1С
 *
 * Предоставляет кэшированный доступ к данным платформы с поддержкой:
 * - Thread-safe операций
 * - Lazy loading
 * - Coroutines интеграции
 * - Улучшенной обработки ошибок
 */
@Service
class PlatformContextService(
    private val contextLoader: PlatformContextLoader
) {
    companion object {
        private val log = LoggerFactory.getLogger(PlatformContextService::class.java)
    }

    @Value("\${platform.context.path:}")
    private lateinit var platformContextPath: String

    private val lock = ReentrantReadWriteLock()
    private var cachedProvider: ContextProvider? = null
    private val isLoaded = AtomicBoolean(false)

    /**
     * Получает провайдер контекста платформы.
     * При первом обращении загружает данные из файлов платформы.
     *
     * @return провайдер контекста платформы
     * @throws IllegalStateException если путь к контексту не настроен
     * @throws RuntimeException если не удалось загрузить контекст
     */
    fun getContextProvider(): ContextProvider {
        // Быстрая проверка с read lock
        lock.read {
            if (isLoaded.get() && cachedProvider != null) {
                return cachedProvider!!
            }
        }

        // Инициализация с write lock
        return lock.write {
            // Двойная проверка в write lock
            if (isLoaded.get() && cachedProvider != null) {
                cachedProvider!!
            } else {
                loadPlatformContext()
                cachedProvider!!
            }
        }
    }

    /**
     * Асинхронное получение провайдера контекста
     */
    suspend fun getContextProviderAsync(): ContextProvider = withContext(Dispatchers.IO) {
        getContextProvider()
    }

    /**
     * Проверяет, инициализирован ли контекст
     */
    fun isContextLoaded(): Boolean = isLoaded.get()

    /**
     * Принудительная перезагрузка контекста
     */
    fun reloadContext() {
        lock.write {
            isLoaded.set(false)
            cachedProvider = null
            loadPlatformContext()
        }
    }

    /**
     * Загружает контекст платформы
     */
    private fun loadPlatformContext() {
        try {
            validateConfigurationPath()

            val path = Paths.get(platformContextPath)
            log.info("Загрузка контекста платформы из: $path")

            cachedProvider = contextLoader.loadPlatformContext(path)
            isLoaded.set(true)

            log.info("Контекст платформы успешно загружен и кэширован")

        } catch (e: Exception) {
            log.error("Ошибка при загрузке контекста платформы", e)
            throw RuntimeException("Не удалось загрузить контекст платформы: ${e.message}", e)
        }
    }

    /**
     * Валидация пути к конфигурации
     */
    private fun validateConfigurationPath() {
        if (!::platformContextPath.isInitialized || platformContextPath.isBlank()) {
            throw IllegalStateException(
                """
                Путь к контексту платформы не настроен.
                Установите свойство platform.context.path в application.yml:
                
                platform:
                  context:
                    path: /path/to/your/platform/context
                """.trimIndent()
            )
        }
    }

    /**
     * Устанавливает путь к контексту программно (для тестирования)
     */
    fun setPlatformContextPath(path: String) {
        lock.write {
            platformContextPath = path
            if (isLoaded.get()) {
                reloadContext()
            }
        }
    }

    /**
     * Получение статистики кэша
     */
    fun getCacheStats(): CacheStats = lock.read {
        CacheStats(
            isLoaded = isLoaded.get(),
            configPath = if (::platformContextPath.isInitialized) platformContextPath else "Не настроен",
            hasProvider = cachedProvider != null
        )
    }

    /**
     * Статистика кэша
     */
    data class CacheStats(
        val isLoaded: Boolean,
        val configPath: String,
        val hasProvider: Boolean
    )
} 