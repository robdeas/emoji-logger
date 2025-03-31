package tech.robd.emojilogger

import org.slf4j.Marker
import org.slf4j.MarkerFactory

/**
 * Enum-based symbolic logging system using emojis to categorise log messages.
 * Designed to improve developer log readability, especially during dev/test/debug.
 */
enum class LogSymbol(val emoji: String, val defaultMarker: String? = null) {

    // ✅ Success / Failure
    GREEN_TICK("✅"),       // U+2705
    RED_CROSS("❌"),        // U+274C
    WARNING_SIGN("⚠️"),     // U+26A0 U+FE0F
    STOP_SIGN("🛑"),        // U+1F6D1

    // ⚙️ Configuration
    CONFIG("⚙️"),           // U+2699 U+FE0F
    PROPERTIES("🪪"),        // U+1FAAA
    CONFIG_FILE("📜"),       // U+1F4DC
    TOOLING("🧰"),           // U+1F9F0
    AUTOWIRE("🧷"),          // U+1F9F7
    DEPENDENCY("🧬"),        // U+1F9EC

    // 📦 Beans / Wiring
    BEAN("📦"),              // U+1F4E6
    HOOK("🪝"),              // U+1FA9D
    BEAN_WIRING("🪛"),       // U+1FA9B

    // 🌐 Network & Security
    NETWORK("🌐"),           // U+1F310
    REMOTE("🌍"),            // U+1F30D
    SEND("📤"),              // U+1F4E4
    TOKEN("🔑"),             // U+1F511
    SECURE("🔐"),            // U+1F510
    AUTH("🛡️"),             // U+1F6E1 U+FE0F
    USER("👤"),              // U+1F464
    ROLE_CHECK("🧑‍⚖️"),     // U+1F9D1 U+200D U+2696 U+FE0F
    AUDIT("🕵️‍♂️"),           // U+1F575 U+FE0F U+200D U+2642 U+FE0F

    // 🧠 REST / Controllers / Routing
    HANDLER("🧑‍💻"),         // U+1F9D1 U+200D U+1F4BB
    ROUTE("🪧"),              // U+1FAE7
    PAGE("📄"),              // U+1F4C4
    RESPONSE("🧾"),          // U+1F9FE
    MAPPING_HIT("🎯"),        // U+1F3AF
    MESSAGE_SEND("✉️"),      // U+2709 U+FE0F
    EVENT_PUBLISH("📣"),     // U+1F4E3

    // 🗄️ Database
    DB("🗄️"),                // U+1F5C4 U+FE0F
    DB_READ("📥"),            // U+1F4E5
    DB_WRITE("📤"),           // U+1F4E4
    DB_QUERY("📊"),           // U+1F4CA
    DB_SCHEMA("🏗️"),          // U+1F3D7 U+FE0F

    // 📡 Messaging / Async
    MESSAGE_RECEIVE("📬"),     // U+1F4EC
    BROADCAST("📡"),           // U+1F4E1
    LISTENER("📶"),            // U+1F4F6

    // 🧪 Debugging / Testing / Trace
    DEBUG("🔍"),               // U+1F50D
    TRACE("🔎"),               // U+1F50E
    TEST("🧪"),                // U+1F9EA
    BUG("🐛"),                 // U+1F41B
    FLOW("🧭"),                // U+1F9ED
    BREAKPOINT("🎯"),          // U+1F3AF
    TEMP_BREAKPOINT("🧨"),     // U+1F9E8
    TEMP_DEBUG("🚧"),          // U+1F6A7
    FIXME("🛠️"),              // U+1F6E0 U+FE0F
    SANITIZE("🧼"),            // U+1F9FC

    // 🧵 Threading / Scheduling
    THREAD("🧵"),              // U+1F9F5
    TIMING("⏱️"),              // U+23F1 U+FE0F
    SCHEDULE("⏰"),             // U+23F0
    FAILSAFE("🧯"),            // U+1F9EF
    WAITING("💤"),             // U+1F4A4
    CRON("📆"),                // U+1F4C6

    // 🚀 Lifecycle
    APP_STARTUP("🌱"),         // U+1F331
    CLEANUP("🧹"),              // U+1F9F9
    RELOAD("🔁"),              // U+1F501
    SHUTDOWN("☠️"),            // U+2620 U+FE0F

    // 📈 Monitoring / Alerts
    METRICS("📈"),             // U+1F4C8
    ALERT("🚨"),               // U+1F6A8

    // 📝 TODO / Fix
    TODO("📝");                // U+1F4DD

    override fun toString(): String = emoji

    fun marker(): Marker = MarkerFactory.getMarker(name)
}
