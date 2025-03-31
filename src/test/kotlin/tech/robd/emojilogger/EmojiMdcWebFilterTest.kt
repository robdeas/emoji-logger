package tech.robd.emojilogger

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.core.env.Environment
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class EmojiMdcWebFilterTest {

    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger
    private lateinit var emojiLogger: EmojiLoggerImpl
    private lateinit var webFilter: EmojiMdcWebFilter

    @BeforeEach
    fun setup() {
        // Set up logger capture
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()

        logger = loggerContext.getLogger(EmojiMdcWebFilterTest::class.java) as Logger
        logger.level = Level.TRACE
        logger.addAppender(listAppender)

        emojiLogger = EmojiLogger.get(EmojiMdcWebFilterTest::class.java)

        // Create the filter
        webFilter = EmojiMdcWebFilter()

        // Clear MDC before each test
        MDC.clear()
    }

    @AfterEach
    fun tearDown() {
        logger.detachAppender(listAppender)
        listAppender.stop()
        MDC.clear()
    }

    @Test
    fun `test web filter adds MDC context and clears after request`() {
        // Setup
        val request = MockHttpServletRequest()
        request.method = "GET"
        request.requestURI = "/api/test"
        request.remoteAddr = "127.0.0.1"

        val response = MockHttpServletResponse()

        val filterChain = mock(FilterChain::class.java)

        // Execute filter
        // Then in your test
        val webFilter = TestableEmojiMdcWebFilter()
        webFilter.doFilterInternal(request, response, filterChain)

        // Verify filterChain was called
        verify(filterChain, times(1)).doFilter(request, response)

        // Verify MDC was cleared after request
        assertNull(MDC.get("requestId"))
        assertNull(MDC.get("path"))
        assertNull(MDC.get("method"))
        assertNull(MDC.get("remoteAddr"))
    }

    @Test
    fun `test filter adds correct MDC values`() {
        // Setup
        val request = MockHttpServletRequest()
        request.method = "POST"
        request.requestURI = "/api/users"
        request.remoteAddr = "192.168.1.100"

        val response = MockHttpServletResponse()

        // Use a custom filter chain that logs with MDC to verify values
        val filterChain = FilterChain { _, _ ->
            assertNotNull(MDC.get("requestId"))
            assertEquals("/api/users", MDC.get("path"))
            assertEquals("POST", MDC.get("method"))
            assertEquals("192.168.1.100", MDC.get("remoteAddr"))

            emojiLogger.info(LogSymbol.HANDLER, "Request processing")
        }

        // Execute filter
        // Then in your test
        val webFilter = TestableEmojiMdcWebFilter()
        webFilter.doFilterInternal(request, response, filterChain)

        // Verify log was captured with MDC values
        val events = listAppender.list
        assertEquals(1, events.size)
        assertEquals("🧑‍💻 Request processing", events[0].message)
        assertEquals("/api/users", events[0].mdcPropertyMap["path"])
        assertEquals("POST", events[0].mdcPropertyMap["method"])
        assertEquals("192.168.1.100", events[0].mdcPropertyMap["remoteAddr"])
        assertNotNull(events[0].mdcPropertyMap["requestId"])
    }
}

class FullMdcInitializerTest {

    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: Logger

    @BeforeEach
    fun setup() {
        // Set up logger capture
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()

        logger = loggerContext.getLogger(FullMdcInitializer::class.java) as Logger
        logger.level = Level.TRACE
        logger.addAppender(listAppender)

        // Clear MDC before each test
        MDC.clear()
    }

    @AfterEach
    fun tearDown() {
        logger.detachAppender(listAppender)
        listAppender.stop()
        MDC.clear()
    }

    @Test
    fun `test MDC enrichment on application ready event`() {
        // Setup mocks
        val env = mock(Environment::class.java)
        `when`(env.getProperty("spring.application.name")).thenReturn("test-app")
        `when`(env.activeProfiles).thenReturn(arrayOf("dev", "local"))

        val props = EmojiLoggingProperties(enableSpringMdc = true, enableWebMdc = true)

        val initializer = FullMdcInitializer(props, env)

        // Mock application event
        val event = mock(ApplicationReadyEvent::class.java)

        // Trigger the event
        initializer.onApplicationEvent(event)

        // Verify MDC was populated
        assertEquals("test-app", MDC.get("app"))
        assertNotNull(MDC.get("pid"))
        assertEquals("dev,local", MDC.get("profile"))
        assertEquals("ready", MDC.get("initPhase"))

        // Verify logs were created
        val events = listAppender.list
        assertTrue(events.size >= 2)

        // Find the specific log messages we expect
        val configLog = events.find { it.message.contains("App Name") }
        val timingLog = events.find { it.message.contains("PID") }

        assertNotNull(configLog)
        assertNotNull(timingLog)
        assertTrue(configLog!!.message.startsWith("⚙️"))
        assertTrue(timingLog!!.message.startsWith("⏱️"))
    }

    @Test
    fun `test initializer respects disabled flag`() {
        // Setup with logging disabled
        val env = mock(Environment::class.java)
        val props = EmojiLoggingProperties(enableSpringMdc = false)

        val initializer = FullMdcInitializer(props, env)

        // Mock application event
        val event = mock(ApplicationReadyEvent::class.java)

        // Trigger the event
        initializer.onApplicationEvent(event)

        // Verify no logs were created
        val events = listAppender.list
        assertEquals(0, events.size)

        // Verify MDC wasn't populated
        assertNull(MDC.get("app"))
        assertNull(MDC.get("profile"))
    }
}

class EmojiLoggerAutoConfigurationTest {

    @Test
    fun `test auto configuration creates beans`() {
        val config = EmojiLoggerAutoConfiguration()

        // Mock environment and properties
        val env = mock(Environment::class.java)
        val props = EmojiLoggingProperties()

        // Get beans
        val mdcInitializer = config.fullMdcInitializer(env, props)
        val webFilter = config.emojiMdcWebFilter()

        // Verify beans were created
        assertNotNull(mdcInitializer)
        assertNotNull(webFilter)
        assertTrue(mdcInitializer is FullMdcInitializer)
        assertTrue(webFilter is EmojiMdcWebFilter)
    }
}

class TestableEmojiMdcWebFilter : EmojiMdcWebFilter() {
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        super.doFilterInternal(request, response, filterChain)
    }
}
