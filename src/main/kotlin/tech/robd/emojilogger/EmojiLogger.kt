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

import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlin.coroutines.EmptyCoroutineContext

/**
 * EmojiLogger provides a Kotlin-friendly DSL for emoji-enhanced logging and MDC context helpers.
 * For SLF4J-compatible logging, prefer [EmojiLoggerImpl].
 */
object EmojiLogger {

    /**
     * Get an [EmojiLoggerImpl] for the given class.
     */
    fun get(clazz: Class<*>): EmojiLoggerImpl =
        EmojiLoggerImpl(LoggerFactory.getLogger(clazz))

    /**
     * Inline reified shortcut to get a logger for a class.
     */
    inline fun <reified T> get(): EmojiLoggerImpl =
        get(T::class.java)

    /**
     * Run a block with MDC values set for its duration (thread-local).
     */
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

    /**
     * Run a suspend block with MDC values in coroutine context.
     */
    suspend inline fun <T> withMdcSuspend(
        vararg entries: Pair<String, String>,
        crossinline block: suspend () -> T
    ): T {
        val original = MDC.getCopyOfContextMap() ?: emptyMap()
        return try {
            entries.forEach { (k, v) -> MDC.put(k, v) }
            withContext(EmptyCoroutineContext) {
                block()
            }
        } finally {
            MDC.clear()
            original.forEach { (k, v) -> MDC.put(k, v) }
        }
    }
}
