package tech.robd.emojilogger

import org.slf4j.MDC

import org.slf4j.LoggerFactory

/**
 * Optional log diagnostics to print PID or startup information
 * outside of Spring context. Useful for CLI or script environments.
 */
object LogStartupDiagnostics {

    fun bootstrap() {
        val pid = ProcessHandle.current().pid().toString()
        val logger = LoggerFactory.getLogger(this::class.java)

        logger.info("[LOG-BOOT] EmojiLogger bootstrap starting")
        logger.info("[LOG-BOOT] PID: {}", pid)
    }
}
