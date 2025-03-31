package tech.robd.emojilogger

import org.slf4j.MDC
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment

class FullMdcInitializer(
    private val props: EmojiLoggingProperties,
    private val env: Environment
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = EmojiLogger.get(this::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        if (!props.enableSpringMdc) return

        MDC.put("app", env.getProperty("spring.application.name") ?: "emoji-app")
        MDC.put("pid", ProcessHandle.current().pid().toString())
        MDC.put("profile", env.activeProfiles.joinToString(",").ifEmpty { "default" })
        MDC.put("initPhase", "ready")

        logger.info(LogSymbol.CONFIG, "App Name: {}", MDC.get("app"))
        logger.info(LogSymbol.TIMING, "PID: {}", MDC.get("pid"))
        logger.debug(LogSymbol.PROPERTIES, "Active profiles: {}", MDC.get("profile"))
        logger.debug(LogSymbol.TEST, "Emoji Logging Enabled: {}", props.enableSpringMdc)
    }
}
