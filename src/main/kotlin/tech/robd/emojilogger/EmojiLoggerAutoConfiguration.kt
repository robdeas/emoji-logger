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

import jakarta.servlet.Filter
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment


@Configuration
@EnableConfigurationProperties(EmojiLoggingProperties::class)
open class EmojiLoggerAutoConfiguration {

    /**
     * Registers MDC initializer for post-startup Spring Boot MDC enrichment.
     */
    @Bean
    open fun fullMdcInitializer(
        env: Environment,
        props: EmojiLoggingProperties
    ): ApplicationListener<ApplicationReadyEvent> =
        FullMdcInitializer(props, env)

    /**
     * Registers web request filter that injects request-specific MDC fields like path, method, etc.
     */
    @Bean
    @ConditionalOnWebApplication
    open fun emojiMdcWebFilter(): Filter = EmojiMdcWebFilter()
}
