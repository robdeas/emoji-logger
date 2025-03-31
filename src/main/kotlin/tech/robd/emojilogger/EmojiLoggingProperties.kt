package tech.robd.emojilogger

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "emoji.logging")
data class EmojiLoggingProperties(
    var enableWebMdc: Boolean = true,
    var enableSpringMdc: Boolean = true
)
