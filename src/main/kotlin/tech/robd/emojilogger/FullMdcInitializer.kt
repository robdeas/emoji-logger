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
