package tech.robd.emojilogger


import kotlinx.coroutines.withContext
import org.slf4j.*
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.withContext

/**
 * Top-level emoji logger for fast usage.
 * Resolves the calling class and logs with emoji.
 */
fun log(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    marker: String? = null,
    message: () -> String
) {
    val className = Throwable().stackTrace
        .firstOrNull {
            !it.className.startsWith("tech.robd.emoji")
        }?.className ?: "UNKNOWN"

    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${symbol.emoji} ${message()}"
    val resolvedMarker = resolveMarker(symbol, marker)

    when (level) {
        LogLevel.INFO -> resolvedMarker?.let { logger.info(it, emojiMessage) } ?: logger.info(emojiMessage)
        LogLevel.DEBUG -> resolvedMarker?.let { logger.debug(it, emojiMessage) } ?: logger.debug(emojiMessage)
        LogLevel.WARN -> resolvedMarker?.let { logger.warn(it, emojiMessage) } ?: logger.warn(emojiMessage)
        LogLevel.ERROR -> resolvedMarker?.let { logger.error(it, emojiMessage) } ?: logger.error(emojiMessage)
        LogLevel.TRACE -> resolvedMarker?.let { logger.trace(it, emojiMessage) } ?: logger.trace(emojiMessage)
    }
}


inline fun EmojiLoggerImpl.log(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    message: () -> String
) = this(symbol, level, message)

fun Logger.emoji(symbol: LogSymbol, message: String, vararg args: Any?) {
    this.info("${symbol.emoji} $message", *args)
}

inline fun <T> withMdc(vararg entries: Pair<String, String>, block: () -> T): T {
    val original = MDC.getCopyOfContextMap() ?: emptyMap()
    try {
        entries.forEach { (k, v) -> MDC.put(k, v) }
        return block()
    } finally {
        MDC.clear()
        original.forEach { (k, v) -> MDC.put(k, v) }
    }
}

suspend inline fun <T> withMdcSuspend(vararg entries: Pair<String, String>, crossinline block: suspend () -> T): T {
    val original = MDC.getCopyOfContextMap() ?: emptyMap()
    return try {
        entries.forEach { (k, v) -> MDC.put(k, v) }
        withContext(EmptyCoroutineContext) { block() }
    } finally {
        MDC.clear()
        original.forEach { (k, v) -> MDC.put(k, v) }
    }
}

/**
 * Resolves a marker from:
 * - override value (if provided)
 * - LogSymbol.defaultMarker (if present)
 * - fallback to LogSymbol.name
 * - returns null if no symbol and no marker
 */
fun resolveMarker(symbol: LogSymbol?, markerOverride: String?): Marker? {
    return when {
        markerOverride != null -> MarkerFactory.getMarker(markerOverride)
        symbol != null -> MarkerFactory.getMarker(symbol.defaultMarker ?: symbol.name)
        else -> null
    }
}

/**
 * Extensions for more natural logging syntax using infix functions.
 */

// Simple infix function for most common case (info level)
infix fun LogSymbol.log(message: String) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.info("${this.emoji} $message")
}

// Infix function that accepts log level
infix fun LogSymbol.logAt(pair: Pair<LogLevel, String>) {
    val (level, message) = pair
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${this.emoji} $message"

    when (level) {
        LogLevel.INFO -> logger.info(emojiMessage)
        LogLevel.DEBUG -> logger.debug(emojiMessage)
        LogLevel.WARN -> logger.warn(emojiMessage)
        LogLevel.ERROR -> logger.error(emojiMessage)
        LogLevel.TRACE -> logger.trace(emojiMessage)
    }
}

// Version with marker support
infix fun LogSymbol.logWithMarker(triple: Triple<LogLevel, String, String?>) {
    val (level, message, markerName) = triple
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${this.emoji} $message"
    val marker = markerName?.let { resolveMarker(this, it) } ?: this.marker()

    when (level) {
        LogLevel.INFO -> logger.info(marker, emojiMessage)
        LogLevel.DEBUG -> logger.debug(marker, emojiMessage)
        LogLevel.WARN -> logger.warn(marker, emojiMessage)
        LogLevel.ERROR -> logger.error(marker, emojiMessage)
        LogLevel.TRACE -> logger.trace(marker, emojiMessage)
    }
}

// For parameterized messages
infix fun LogSymbol.logFormat(messageWithArgs: Pair<String, Array<out Any?>>) {
    val (messageTemplate, args) = messageWithArgs
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.info("${this.emoji} $messageTemplate", *args)
}

// Support for parameterized messages with log level
infix fun LogSymbol.logFormatAt(params: LogParams) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${this.emoji} ${params.messageTemplate}"

    when (params.level) {
        LogLevel.INFO -> logger.info(emojiMessage, *params.args)
        LogLevel.DEBUG -> logger.debug(emojiMessage, *params.args)
        LogLevel.WARN -> logger.warn(emojiMessage, *params.args)
        LogLevel.ERROR -> logger.error(emojiMessage, *params.args)
        LogLevel.TRACE -> logger.trace(emojiMessage, *params.args)
    }
}

// Data class for holding log parameters
data class LogParams(
    val level: LogLevel,
    val messageTemplate: String,
    val args: Array<out Any?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LogParams

        if (level != other.level) return false
        if (messageTemplate != other.messageTemplate) return false
        if (!args.contentEquals(other.args)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + messageTemplate.hashCode()
        result = 31 * result + args.contentHashCode()
        return result
    }
}

// Helper functions to create params
fun params(level: LogLevel, messageTemplate: String, vararg args: Any?) =
    LogParams(level, messageTemplate, args)

// DSL-style extension functions for LogSymbol to support lambda message creation
fun LogSymbol.debug(message: () -> String) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.debug("${this.emoji} ${message()}")
}

fun LogSymbol.info(message: () -> String) {
    val logger = CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.info("${this.emoji} ${message()}")
}

fun LogSymbol.warn(message: () -> String) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.warn("${this.emoji} ${message()}")
}

fun LogSymbol.error(message: () -> String) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.error("${this.emoji} ${message()}")
}

fun LogSymbol.trace(message: () -> String) {
    val logger =  CallerResolverFactory.getResolver().getLoggerForCaller()
    logger.trace("${this.emoji} ${message()}")
}

// Extension properties for LogLevel to make the API more fluent
val info get() = LogLevel.INFO
val debug get() = LogLevel.DEBUG
val warn get() = LogLevel.WARN
val error get() = LogLevel.ERROR
val trace get() = LogLevel.TRACE

/**
 * Additional extension functions to enhance the infix logging approach
 * with Result types, flow control, and performance measurement.
 */

// Result type extensions
inline fun <T> Result<T>.logOnSuccess(symbol: LogSymbol, crossinline message: (T) -> String): Result<T> =
    onSuccess { value ->
        symbol log message(value)
    }

inline fun <T> Result<T>.logOnFailure(symbol: LogSymbol, crossinline message: (Throwable) -> String): Result<T> =
    onFailure { throwable ->
        symbol logAt (error to message(throwable))
    }

/**
 * Extension function to add logs to any object
 */
fun <T : Any> T.logIt(symbol: LogSymbol, message: String): T {
    symbol log "$message: $this"
    return this
}

/**
 * Emoji logging DSL with optional MDC context.
 *
 * Example:
 *   log(LogSymbol.CONFIG, mdc = mapOf("profile" to "dev")) { "Config loaded" }
 */
inline fun emojiLog(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    mdc: Map<String, String> = emptyMap(),
    message: () -> String
) {
    val logger = CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${symbol.emoji} ${message()}"
    val resolvedMarker = resolveMarker(symbol, null)

    // Set and clear MDC if needed
    val originalMdc = MDC.getCopyOfContextMap() ?: emptyMap()
    try {
        mdc.forEach { (k, v) -> MDC.put(k, v) }

        when (level) {
            LogLevel.INFO -> resolvedMarker?.let { logger.info(it, emojiMessage) } ?: logger.info(emojiMessage)
            LogLevel.DEBUG -> resolvedMarker?.let { logger.debug(it, emojiMessage) } ?: logger.debug(emojiMessage)
            LogLevel.WARN -> resolvedMarker?.let { logger.warn(it, emojiMessage) } ?: logger.warn(emojiMessage)
            LogLevel.ERROR -> resolvedMarker?.let { logger.error(it, emojiMessage) } ?: logger.error(emojiMessage)
            LogLevel.TRACE -> resolvedMarker?.let { logger.trace(it, emojiMessage) } ?: logger.trace(emojiMessage)
        }
    } finally {
        MDC.clear()
        originalMdc.forEach { (k, v) -> MDC.put(k, v) }
    }
}



/**
 * Suspend-compatible emoji logging DSL with MDC support.
 *
 * Example:
 *   suspendLog(LogSymbol.API, mdc = mapOf("requestId" to "abc")) {
 *       "Handled request"
 *   }
 */
suspend inline fun suspendLog(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    mdc: Map<String, String> = emptyMap(),
    crossinline message: suspend () -> String
) {
    val logger = CallerResolverFactory.getResolver().getLoggerForCaller()
    val resolvedMarker = resolveMarker(symbol, null)

    val originalMdc = MDC.getCopyOfContextMap() ?: emptyMap()
    try {
        mdc.forEach { (k, v) -> MDC.put(k, v) }

        val emojiMessage = withContext(EmptyCoroutineContext) { "${symbol.emoji} ${message()}" }

        when (level) {
            LogLevel.INFO -> resolvedMarker?.let { logger.info(it, emojiMessage) } ?: logger.info(emojiMessage)
            LogLevel.DEBUG -> resolvedMarker?.let { logger.debug(it, emojiMessage) } ?: logger.debug(emojiMessage)
            LogLevel.WARN -> resolvedMarker?.let { logger.warn(it, emojiMessage) } ?: logger.warn(emojiMessage)
            LogLevel.ERROR -> resolvedMarker?.let { logger.error(it, emojiMessage) } ?: logger.error(emojiMessage)
            LogLevel.TRACE -> resolvedMarker?.let { logger.trace(it, emojiMessage) } ?: logger.trace(emojiMessage)
        }
    } finally {
        MDC.clear()
        originalMdc.forEach { (k, v) -> MDC.put(k, v) }
    }
}

inline operator fun EmojiLoggerImpl.invoke(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    message: () -> String
) {
    when (level) {
        LogLevel.INFO -> this.info(symbol, message())
        LogLevel.DEBUG -> this.debug(symbol, message())
        LogLevel.WARN -> this.warn(symbol, message())
        LogLevel.ERROR -> this.error(symbol, message())
        LogLevel.TRACE -> this.trace(symbol, message())
    }
}

@JvmName("emojiLogSimple")
inline fun emojiLog(
    symbol: LogSymbol,
    level: LogLevel = LogLevel.INFO,
    message: () -> String
) {
    val logger = CallerResolverFactory.getResolver().getLoggerForCaller()
    val emojiMessage = "${symbol.emoji} ${message()}"
    val resolvedMarker = resolveMarker(symbol, null)

    when (level) {
        LogLevel.INFO -> resolvedMarker?.let { logger.info(it, emojiMessage) } ?: logger.info(emojiMessage)
        LogLevel.DEBUG -> resolvedMarker?.let { logger.debug(it, emojiMessage) } ?: logger.debug(emojiMessage)
        LogLevel.WARN -> resolvedMarker?.let { logger.warn(it, emojiMessage) } ?: logger.warn(emojiMessage)
        LogLevel.ERROR -> resolvedMarker?.let { logger.error(it, emojiMessage) } ?: logger.error(emojiMessage)
        LogLevel.TRACE -> resolvedMarker?.let { logger.trace(it, emojiMessage) } ?: logger.trace(emojiMessage)
    }
}