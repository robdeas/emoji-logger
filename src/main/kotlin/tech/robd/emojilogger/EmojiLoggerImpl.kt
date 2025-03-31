/*
 * Copyright 2025 Rob Deas
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.robd.emojilogger

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * EmojiLoggerImpl can act in two modes:
 * 1. Smart (default): dynamically resolves logger using CallerResolver at every log call.
 * 2. Bound: if a Logger is explicitly passed in, uses that Logger statically.
 *
 * This ensures flexible integration with Spring and structured logging setups.
 */
class EmojiLoggerImpl(
    private val boundLogger: Logger? = null
) : Logger {

    private fun resolved(): Logger =
        boundLogger ?: CallerResolverFactory.getResolver().getLoggerForCaller()

    private fun format(symbol: LogSymbol, message: String) = "${symbol.emoji} $message"
    private fun marker(symbol: LogSymbol): Marker = MarkerFactory.getMarker(symbol.name)

    fun withMdc(vararg entries: Pair<String, String>, block: () -> Unit) {
        val original = MDC.getCopyOfContextMap() ?: emptyMap()
        try {
            entries.forEach { (k, v) -> MDC.put(k, v) }
            block()
        } finally {
            MDC.clear()
            original.forEach { (k, v) -> MDC.put(k, v) }
        }
    }

    suspend fun withMdcSuspend(vararg entries: Pair<String, String>, block: suspend () -> Unit) {
        val original = MDC.getCopyOfContextMap() ?: emptyMap()
        try {
            entries.forEach { (k, v) -> MDC.put(k, v) }
            block()
        } finally {
            MDC.clear()
            original.forEach { (k, v) -> MDC.put(k, v) }
        }
    }

    fun log(symbol: LogSymbol, level: LogLevel, message: String, vararg args: Any?) {
        when (level) {
            LogLevel.INFO -> info(symbol, message, *args)
            LogLevel.DEBUG -> debug(symbol, message, *args)
            LogLevel.WARN -> warn(symbol, message, *args)
            LogLevel.ERROR -> error(symbol, message, *args)
            LogLevel.TRACE -> trace(symbol, message, *args)
        }
    }


    /* === Emoji log methods === */

    fun info(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().info(format(symbol, message), *args)

    fun debug(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().debug(format(symbol, message), *args)

    fun warn(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().warn(format(symbol, message), *args)

    fun error(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().error(format(symbol, message), *args)

    fun trace(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().trace(format(symbol, message), *args)

    fun infoWithMarker(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().info(marker(symbol), format(symbol, message), *args)

    fun debugWithMarker(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().debug(marker(symbol), format(symbol, message), *args)

    fun warnWithMarker(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().warn(marker(symbol), format(symbol, message), *args)

    fun errorWithMarker(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().error(marker(symbol), format(symbol, message), *args)

    fun traceWithMarker(symbol: LogSymbol, message: String, vararg args: Any?) =
        resolved().trace(marker(symbol), format(symbol, message), *args)

    /* === SLF4J Logger methods === */

    override fun getName(): String = resolved().name

    override fun isTraceEnabled() = resolved().isTraceEnabled
    override fun isDebugEnabled() = resolved().isDebugEnabled
    override fun isInfoEnabled() = resolved().isInfoEnabled
    override fun isWarnEnabled() = resolved().isWarnEnabled
    override fun isErrorEnabled() = resolved().isErrorEnabled

    override fun trace(msg: String?) = resolved().trace(msg)
    override fun debug(msg: String?) = resolved().debug(msg)
    override fun info(msg: String?) = resolved().info(msg)
    override fun warn(msg: String?) = resolved().warn(msg)
    override fun error(msg: String?) = resolved().error(msg)

    override fun trace(format: String?, arg: Any?) = resolved().trace(format, arg)
    override fun debug(format: String?, arg: Any?) = resolved().debug(format, arg)
    override fun info(format: String?, arg: Any?) = resolved().info(format, arg)
    override fun warn(format: String?, arg: Any?) = resolved().warn(format, arg)
    override fun error(format: String?, arg: Any?) = resolved().error(format, arg)

    override fun trace(format: String?, arg1: Any?, arg2: Any?) = resolved().trace(format, arg1, arg2)
    override fun debug(format: String?, arg1: Any?, arg2: Any?) = resolved().debug(format, arg1, arg2)
    override fun info(format: String?, arg1: Any?, arg2: Any?) = resolved().info(format, arg1, arg2)
    override fun warn(format: String?, arg1: Any?, arg2: Any?) = resolved().warn(format, arg1, arg2)
    override fun error(format: String?, arg1: Any?, arg2: Any?) = resolved().error(format, arg1, arg2)

    override fun trace(format: String?, vararg arguments: Any?) = resolved().trace(format, *arguments)
    override fun debug(format: String?, vararg arguments: Any?) = resolved().debug(format, *arguments)
    override fun info(format: String?, vararg arguments: Any?) = resolved().info(format, *arguments)
    override fun warn(format: String?, vararg arguments: Any?) = resolved().warn(format, *arguments)
    override fun error(format: String?, vararg arguments: Any?) = resolved().error(format, *arguments)

    override fun trace(msg: String?, t: Throwable?) = resolved().trace(msg, t)
    override fun debug(msg: String?, t: Throwable?) = resolved().debug(msg, t)
    override fun info(msg: String?, t: Throwable?) = resolved().info(msg, t)
    override fun warn(msg: String?, t: Throwable?) = resolved().warn(msg, t)
    override fun error(msg: String?, t: Throwable?) = resolved().error(msg, t)

    override fun isTraceEnabled(marker: Marker?) = resolved().isTraceEnabled(marker)
    override fun isDebugEnabled(marker: Marker?) = resolved().isDebugEnabled(marker)
    override fun isInfoEnabled(marker: Marker?) = resolved().isInfoEnabled(marker)
    override fun isWarnEnabled(marker: Marker?) = resolved().isWarnEnabled(marker)
    override fun isErrorEnabled(marker: Marker?) = resolved().isErrorEnabled(marker)

    override fun trace(marker: Marker?, msg: String?) = resolved().trace(marker, msg)
    override fun debug(marker: Marker?, msg: String?) = resolved().debug(marker, msg)
    override fun info(marker: Marker?, msg: String?) = resolved().info(marker, msg)
    override fun warn(marker: Marker?, msg: String?) = resolved().warn(marker, msg)
    override fun error(marker: Marker?, msg: String?) = resolved().error(marker, msg)

    override fun trace(marker: Marker?, format: String?, arg: Any?) = resolved().trace(marker, format, arg)
    override fun debug(marker: Marker?, format: String?, arg: Any?) = resolved().debug(marker, format, arg)
    override fun info(marker: Marker?, format: String?, arg: Any?) = resolved().info(marker, format, arg)
    override fun warn(marker: Marker?, format: String?, arg: Any?) = resolved().warn(marker, format, arg)
    override fun error(marker: Marker?, format: String?, arg: Any?) = resolved().error(marker, format, arg)

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        resolved().trace(marker, format, arg1, arg2)

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        resolved().debug(marker, format, arg1, arg2)

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        resolved().info(marker, format, arg1, arg2)

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        resolved().warn(marker, format, arg1, arg2)

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) =
        resolved().error(marker, format, arg1, arg2)

    override fun trace(marker: Marker?, format: String?, vararg argArray: Any?) =
        resolved().trace(marker, format, *argArray)

    override fun debug(marker: Marker?, format: String?, vararg argArray: Any?) =
        resolved().debug(marker, format, *argArray)

    override fun info(marker: Marker?, format: String?, vararg argArray: Any?) =
        resolved().info(marker, format, *argArray)

    override fun warn(marker: Marker?, format: String?, vararg argArray: Any?) =
        resolved().warn(marker, format, *argArray)

    override fun error(marker: Marker?, format: String?, vararg argArray: Any?) =
        resolved().error(marker, format, *argArray)

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) =
        resolved().trace(marker, msg, t)

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) =
        resolved().debug(marker, msg, t)

    override fun info(marker: Marker?, msg: String?, t: Throwable?) =
        resolved().info(marker, msg, t)

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) =
        resolved().warn(marker, msg, t)

    override fun error(marker: Marker?, msg: String?, t: Throwable?) =
        resolved().error(marker, msg, t)

    companion object {
        @JvmStatic
        fun forClass(clazz: Class<*>): EmojiLoggerImpl =
            EmojiLoggerImpl(LoggerFactory.getLogger(clazz))

        @JvmStatic
        fun forName(name: String): EmojiLoggerImpl =
            EmojiLoggerImpl(LoggerFactory.getLogger(name))
    }
}
