package tech.robd.emojilogger

import org.slf4j.Logger

/**
 * Interface for caller resolution - makes testing easier by allowing mocking
 */
interface ICallerResolver {
    /**
     * Resolves the name of the class that invoked the logging function.
     */
    fun resolveCallingClass(): String

    /**
     * Returns a cached logger for the calling class.
     */
    fun getLoggerForCaller(): Logger
}