package tech.robd.emojilogger

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class CallerResolverTest {

    private val resolver = CallerResolver()

    @Test
    fun `resolve from standard class method`() {
        val className = resolver.resolveCallingClass()
        println("Resolved class: $className")

        assertTrue(className.isNotBlank(), "Should resolve a class name")
        assertFalse(className == "DEFAULT", "Should not fall back to DEFAULT")
        assertFalse(className.startsWith("java.") || className.startsWith("org.junit."), "Should not resolve framework class")
    }

    @Test
    fun `resolve through suspend function`() = runBlocking {
        val className = suspendLoggingEntry()
        println("Resolved suspend class: $className")
        assertTrue(className.contains("CallerResolverTest"))
    }


    @Test
    fun `resolve through inline function`() {
        val className = inlineLoggingEntry { resolver.resolveCallingClass() }
        assertTrue(className.contains("CallerResolverTest"))
    }

    @Test
    fun `class with name including default is not excluded`() {
        val className = DefaultLoggerTest().callLog()
        assertTrue(className.contains("DefaultLoggerTest"))
        assertFalse(className.contains("DEFAULT")) // ensure we didn't hit fallback
    }

    // 🔧 Helper: simulates coroutine context to test stack resolution with suspend
    @Suppress("REDUNDANT_SUSPEND_MODIFIER")
    suspend fun suspendLoggingEntry(): String = resolver.resolveCallingClass()

    inline fun inlineLoggingEntry(block: () -> String): String = block()

    class DefaultLoggerTest {
        fun callLog(): String = CallerResolver().resolveCallingClass()
    }
}
