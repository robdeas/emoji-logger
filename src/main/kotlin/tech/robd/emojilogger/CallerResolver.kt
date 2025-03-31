package tech.robd.emojilogger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * CallerResolver is responsible for determining the class that initiated a log call.
 *
 *  Implements an interface for easier testing and dependency injection.
 *
 * This implementation uses stack trace inspection (via Thread.currentThread().stackTrace)
 * to walk up the call stack and identify the first non-framework, non-logging class.
 *
 * ⚙️ Performance Considerations:
 * - The result is cached per call site (stack frame index) for high-performance reuse.
 * - Initial resolution performs a short stack walk (~5-15 frames) which is fast in modern JVMs.
 * - All subsequent calls hit the in-memory cache (ConcurrentHashMap) and resolve instantly.
 *
 * 🚀 In practice, this method adds negligible overhead compared to the cost of structured logging itself,
 * even in high-throughput systems. Still, for ultra-hot paths, you may prefer a pre-bound logger:
 *
 *     val logger = EmojiLoggerImpl.forClass(MyService::class.java)
 *
 * This avoids resolution and binds logging directly to the class.
 *
 * ✅ This trade-off is aligned with most modern logging DSLs (e.g., Kotlin Logging, Timber, Log4j2).
 *
 * Supports skipping Kotlin compiler artifacts such as DefaultImpls, suspend functions,
 * inlined functions, and SLF4J / Logback / Kotlinx Coroutine wrappers.
 */
class CallerResolver : ICallerResolver {

    companion object {
        @JvmStatic
        var enableCallerDetection: Boolean = true
    }

    private val loggerCache = ConcurrentHashMap<String, Logger>()

    private fun isSelfFrame(className: String): Boolean =
        className == CallerResolver::class.java.name

    override fun resolveCallingClass(): String {
        if (!enableCallerDetection) return "DEFAULT"

        val stackTrace = Thread.currentThread().stackTrace

        for (frame in stackTrace) {
            val className = frame.className
            if (!isFrameworkClass(className)  && !isSelfFrame(className)) {
                return className
            }
        }

        return "DEFAULT"
    }

    override fun getLoggerForCaller(): Logger {
        val className = resolveCallingClass()
        return loggerCache.computeIfAbsent(className) {
            LoggerFactory.getLogger(it)
        }
    }

    private fun isFrameworkClass(className: String): Boolean {
        return className.run {
                    startsWith("org.slf4j") ||
                    startsWith("ch.qos.logback") ||
                    startsWith("java.") ||
                    startsWith("kotlin.reflect.") ||
                    startsWith("kotlinx.coroutines.") ||
                    startsWith("kotlin.coroutines.") ||
                    contains("DefaultImpls") ||
                    contains("Kt$") ||
                    contains("\$default") ||
                    contains("\$inlined") ||
                    contains("\$accessor") ||
                    contains("\$lambda") ||
                    contains("\$suspendImpl")
        }
    }
}
