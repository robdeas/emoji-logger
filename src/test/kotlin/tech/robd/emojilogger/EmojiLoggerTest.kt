package tech.robd.emojilogger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.firstValue
import org.mockito.kotlin.verify
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class EmojiLoggerTest {

    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger
    private lateinit var emojiLogger: EmojiLoggerImpl

    @BeforeEach
    fun setup() {
        // Get Logback's Logger context
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        // Create and configure the list appender to capture logs
        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()

        // Set up the logger and attach our appender
        logger = loggerContext.getLogger(EmojiLoggerTest::class.java) as Logger
        logger.level = Level.TRACE
        logger.addAppender(listAppender)

        // Create our emoji logger with the underlying logger
        emojiLogger = EmojiLogger.get(EmojiLoggerTest::class.java)

        // Clear MDC before each test
        MDC.clear()
    }

    @BeforeEach
    fun resetResolver() {
        CallerResolverFactory.setResolver(CallerResolver())  // reset default
        CallerResolver.enableCallerDetection = true
    }

    @AfterEach
    fun cleanUpGlobals() {
        CallerResolverFactory.setResolver(CallerResolver())
        CallerResolver.enableCallerDetection = true
    }

    @AfterEach
    fun tearDown() {
        // Clean up
        logger.detachAppender(listAppender)
        listAppender.stop()
        MDC.clear()
    }

    @Test
    fun `test CallerResolver directly`() {
        // Test when enabled
        CallerResolver.enableCallerDetection = true
        val resolvedName1 = CallerResolverFactory.getResolver().getLoggerForCaller()

        // Test when disabled
        CallerResolver.enableCallerDetection = false
        val resolvedName2 = CallerResolverFactory.getResolver().getLoggerForCaller()

        // Verify behavior
        assertNotEquals("DEFAULT", resolvedName1.name, "When enabled, should not return DEFAULT")
        assertEquals("DEFAULT", resolvedName2.name, "When disabled, should return DEFAULT")

        // Restore state
        CallerResolver.enableCallerDetection = true
    }

    @Test
    fun `test log function uses resolver correctly`() {
        val mockResolver = mock(ICallerResolver::class.java)
        val mockLogger = mock(Logger::class.java)

        `when`(mockResolver.resolveCallingClass()).thenReturn("MOCK-CLASS")
        `when`(mockResolver.getLoggerForCaller()).thenReturn(mockLogger)

        CallerResolverFactory.setResolver(mockResolver)

        log(LogSymbol.TEST) { "Test message" }

        val messageCaptor = argumentCaptor<String>()
        val markerCaptor = argumentCaptor<org.slf4j.Marker>()

        verify(mockResolver).getLoggerForCaller()
        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture())

        assertEquals("🧪 Test message", messageCaptor.firstValue)
    }


    @Test
    fun `test basic emoji logging`() {
        // When logging with emoji
        emojiLogger.info(LogSymbol.GREEN_TICK, "Operation successful")
        emojiLogger.debug(LogSymbol.DEBUG, "Debug information")
        emojiLogger.warn(LogSymbol.WARNING_SIGN, "Warning message")
        emojiLogger.error(LogSymbol.STOP_SIGN, "Error occurred")

        // Then we should see the emoji in the messages
        val events = listAppender.list
        assertEquals(4, events.size)

        assertEquals("✅ Operation successful", events[0].message)
        assertEquals(Level.INFO, events[0].level)

        assertEquals("🔍 Debug information", events[1].message)
        assertEquals(Level.DEBUG, events[1].level)

        assertEquals("⚠️ Warning message", events[2].message)
        assertEquals(Level.WARN, events[2].level)

        assertEquals("🛑 Error occurred", events[3].message)
        assertEquals(Level.ERROR, events[3].level)
    }

    @Test
    fun `test marker-based logging`() {
        // When using marker-based logging
        emojiLogger.infoWithMarker(LogSymbol.CONFIG, "System configuration loaded")
        emojiLogger.debugWithMarker(LogSymbol.TRACE, "Tracing execution")

        // Then markers should be set correctly
        val events = listAppender.list
        assertEquals(2, events.size)

        // Check marker using getMarker() instead of markerName
        assertNotNull(events[0].marker)
        assertEquals("CONFIG", events[0].marker.name)
        assertEquals("⚙️ System configuration loaded", events[0].message)

        assertNotNull(events[1].marker)
        assertEquals("TRACE", events[1].marker.name)
        assertEquals("🔎 Tracing execution", events[1].message)
    }

    @Test
    fun `test generic level-based logging`() {
        // When using generic level-based logging
        emojiLogger.log(LogSymbol.APP_STARTUP, LogLevel.INFO, "Application starting")
        emojiLogger.log(LogSymbol.SHUTDOWN, LogLevel.WARN, "Application shutting down")

        // Then the logs should appear with correct level and emoji
        val events = listAppender.list
        assertEquals(2, events.size)

        assertEquals("🌱 Application starting", events[0].message)
        assertEquals(Level.INFO, events[0].level)

        assertEquals("☠️ Application shutting down", events[1].message)
        assertEquals(Level.WARN, events[1].level)
    }

    @Test
    fun `test MDC context helpers`() {
        // When using MDC context helpers
        emojiLogger.withMdc(
            "requestId" to "12345",
            "userId" to "user-abc"
        ) {
            emojiLogger.info(LogSymbol.USER, "User action")
            assertEquals("12345", MDC.get("requestId"))
            assertEquals("user-abc", MDC.get("userId"))
        }

        // Then MDC should be cleared after block execution
        assertNull(MDC.get("requestId"))
        assertNull(MDC.get("userId"))

        // And the log should be present
        val events = listAppender.list
        assertEquals(1, events.size)
        assertEquals("👤 User action", events[0].message)
        assertEquals("12345", events[0].mdcPropertyMap["requestId"])
        assertEquals("user-abc", events[0].mdcPropertyMap["userId"])
    }

    @Test
    fun `test suspend MDC context helpers`() = runBlocking {
        // When using suspend MDC context helpers
        emojiLogger.withMdcSuspend(
            "asyncOperation" to "true",
            "operationId" to "op-789"
        ) {
            assertEquals("true", MDC.get("asyncOperation"))
            assertEquals("op-789", MDC.get("operationId"))
            emojiLogger.info(LogSymbol.THREAD, "Async operation")
        }

        // Then MDC should be cleared after suspend function
        assertNull(MDC.get("asyncOperation"))
        assertNull(MDC.get("operationId"))

        // And the log should be present
        val events = listAppender.list
        assertEquals(1, events.size)
        assertEquals("🧵 Async operation", events[0].message)
        assertEquals("true", events[0].mdcPropertyMap["asyncOperation"])
        assertEquals("op-789", events[0].mdcPropertyMap["operationId"])
    }

    @Test
    fun `test logging extension DSL syntax`() {
        // When using DSL-style logging
        emojiLogger(LogSymbol.DB_QUERY) { "Query executed" }
        emojiLogger(LogSymbol.DB_WRITE, LogLevel.DEBUG) { "Data written" }

        // Then the logs should appear with correct format
        val events = listAppender.list
        assertEquals(2, events.size)

        assertEquals("📊 Query executed", events[0].message)
        assertEquals(Level.INFO, events[0].level)

        assertEquals("📤 Data written", events[1].message)
        assertEquals(Level.DEBUG, events[1].level)
    }

    @Test
    fun `test caller resolution toggle`() {
        // Configure root logger
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        rootLogger.addAppender(listAppender)

        try {
            // Given caller detection is enabled
            CallerResolver.enableCallerDetection = true

            // When using top-level log function
            log(LogSymbol.BEAN) { "Bean created" }

            // Then we should see a message with the emoji
            val events = listAppender.list
            assertEquals(1, events.size)
            assertEquals("📦 Bean created", events[0].message)

            // Store the resolved logger name
            val resolvedName = events[0].loggerName

            // When caller detection is disabled
            listAppender.list.clear()
            CallerResolver.enableCallerDetection = false
            log(LogSymbol.BEAN) { "Another bean" }

            // Then we should get DEFAULT as logger name
            assertEquals(1, listAppender.list.size)
            assertEquals("DEFAULT", listAppender.list[0].loggerName)
            assertEquals("📦 Another bean", listAppender.list[0].message)

            // And it should be different from the resolved name
            assertNotEquals(resolvedName, "DEFAULT",
                "Resolved name should be different from DEFAULT")
        } finally {
            // Make sure to restore state and clean up
            CallerResolver.enableCallerDetection = true
            rootLogger.detachAppender(listAppender)
        }
    }

    @Test
    fun `test logger caching`() {
        // When resolving caller multiple times
        val logger1 = CallerResolverFactory.getResolver().getLoggerForCaller()
        val logger2 = CallerResolverFactory.getResolver().getLoggerForCaller()

        // Then we should get the same logger instance
        assertSame(logger1, logger2)
    }

    @Test
    fun `test log function fallback when class cannot be resolved`() {
        val mockResolver = mock(ICallerResolver::class.java)
        val mockLogger = mock(Logger::class.java)

        `when`(mockResolver.resolveCallingClass()).thenReturn("UNKNOWN")
        `when`(mockResolver.getLoggerForCaller()).thenReturn(mockLogger)

        CallerResolverFactory.setResolver(mockResolver)

        log(LogSymbol.BUG) { "Unresolved log" }

        val markerCaptor = argumentCaptor<org.slf4j.Marker>()
        val messageCaptor = argumentCaptor<String>()

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture())

        assertEquals("${LogSymbol.BUG.emoji} Unresolved log", messageCaptor.firstValue)
        assertEquals("BUG", markerCaptor.firstValue.name)
    }

    @Test
    fun `test log function respects marker override`() {
        val mockResolver = mock(ICallerResolver::class.java)
        val mockLogger = mock(Logger::class.java)

        `when`(mockResolver.getLoggerForCaller()).thenReturn(mockLogger)
        CallerResolverFactory.setResolver(mockResolver)

        log(LogSymbol.BUG, marker = "OVERRIDE") { "Bug with custom marker" }

        val markerCaptor = argumentCaptor<org.slf4j.Marker>()
        val messageCaptor = argumentCaptor<String>()

        verify(mockLogger).info(markerCaptor.capture(), messageCaptor.capture())

        assertEquals("${LogSymbol.BUG.emoji} Bug with custom marker", messageCaptor.firstValue)
        assertEquals("OVERRIDE", markerCaptor.firstValue.name)
    }

    @Test
    fun `test logFormatAt extension`() {
        val mockResolver = mock(ICallerResolver::class.java)
        val mockLogger = mock(Logger::class.java)

        `when`(mockResolver.getLoggerForCaller()).thenReturn(mockLogger)
        CallerResolverFactory.setResolver(mockResolver)

        LogSymbol.DB logFormatAt params(LogLevel.INFO, "Saved item: {}", "123")

        val formatCaptor = argumentCaptor<String>()
        val argsCaptor = argumentCaptor<Array<Any>>()

        // Use raw verify to handle vararg Object[]
        verify(mockLogger).info(formatCaptor.capture(), *argsCaptor.capture())

        assertEquals("${LogSymbol.DB.emoji} Saved item: {}", formatCaptor.firstValue)
        assertEquals("123", argsCaptor.firstValue[0])
    }


    @Test
    fun `test Result logOnSuccess and logOnFailure`() {
        CallerResolverFactory.setResolver(object : ICallerResolver {
            override fun resolveCallingClass(): String = EmojiLoggerTest::class.java.name
            override fun getLoggerForCaller(): org.slf4j.Logger =
                LoggerFactory.getLogger(EmojiLoggerTest::class.java)
        })

        val result = runCatching { "data" }
            .logOnSuccess(LogSymbol.GREEN_TICK) { "Fetched: $it" }
            .logOnFailure(LogSymbol.RED_CROSS) { "Error: ${it.message}" }
        assertTrue(result.isSuccess)
        val events = listAppender.list
        assertTrue(events.any { it.message.contains("✅ Fetched: data") })
    }

    @Test
    fun `test MDC is restored after withMdc`() {
        MDC.put("original", "keep-me")

        emojiLogger.withMdc("temp" to "123") {
            assertEquals("123", MDC.get("temp"))
            assertEquals("keep-me", MDC.get("original"))
        }

        assertNull(MDC.get("temp"))
        assertEquals("keep-me", MDC.get("original"))
    }

    @Test
    fun `test CallerResolver uses cache`() {
        val resolver = CallerResolver()
        val className1 = resolver.resolveCallingClass()
        val className2 = resolver.resolveCallingClass()

        assertEquals(className1, className2)
    }


}


fun <T> org.mockito.ArgumentCaptor<T>.firstValueOrNull(): T? =
    if (allValues.isEmpty()) null else firstValue

// Create this helper class in your test package
class CallerHelper {
    companion object {
        fun makeLogCall(message: String) {
            log(LogSymbol.BEAN) { message }
        }
    }
}

class TestLoggerUser {
    fun logSomething() {
        log(LogSymbol.BEAN) { "Test message" }
    }
}


