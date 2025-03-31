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
