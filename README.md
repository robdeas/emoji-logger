# 🤖 emoji-logger

A Kotlin-native, emoji-enhanced logging utility with support for SLF4J, structured MDC, Spring Boot auto-configuration, and coroutine-aware logging.

Built for developers who value clean, expressive, and context-rich logs — with a dash of fun. 😄

---

## 📚 Table of Contents
- [✨ Features](#-features)
- [🚀 Getting Started](#-getting-started)
- [📁 What is MDC and Why Should You Care?](#-what-is-mdc-and-why-should-you-care)
- [📜 Example Usage](#-example-usage)
- [🌿 Spring Boot Setup](#-spring-boot-setup)
- [🔑 DSL Cheatsheet](#-dsl-cheatsheet)
- [🎨 Emoji Reference](#-emoji-reference)
- [🧱 Using from Java](#-using-from-java)
- [📍 Roadmap](#-roadmap)
- [🤝 Contributing](#-contributing)
- [📄 License](#-license)

---

## ✨ Features

- ✅ **Emoji-based log symbols** via `LogSymbol` enum
- 🧵 **SLF4J-compatible** with marker support
- 🧪 **Scoped MDC support** (`withMdc`, `withMdcSuspend`)
- 🌱 **Spring Boot integration** via auto-config & filters
- 🔌 **Web MDC filter** for `requestId`, path, etc.
- ⏱️ **Early & late MDC phases** (for startup tracing)
- 🧰 **Extension-friendly** Kotlin DSL
- 🩵 **Top-level `log(...)` and `emojiLog(...)` functions**
- 🔍 Caller resolution and dynamic logger support

---

## 🚀 Getting Started

### Add to Gradle

```kotlin

```

<sub>Replace `x.y.z` with the latest published version.</sub>

Or include locally:

```kotlin

```

---

## 📁 What is MDC and Why Should You Care?

Most logging frameworks let you write messages like this:

```kotlin
logger.info("User logged in")
```

But in real-world apps, you often need more context:
- *Which* user?
- *Which* request?
- *Which* service or job?

**MDC (Mapped Diagnostic Context)** lets you inject **key-value pairs** into the logging system — without changing every log call.

It's like adding automatic tags to your logs:

```kotlin
MDC.put("userId", "abc123")
logger.info("User logged in")
// → Logs: userId=abc123 ▶ User logged in
```

### ✅ Why MDC matters:
- Makes logs richer and searchable (great for ELK/Grafana/etc.)
- Lets you tag logs per request, user, or job
- Works *without* needing to change every log line

### ↺ And it’s cleared automatically
With `emoji-logger`, you don’t have to manage MDC manually:

```kotlin
withMdc("requestId" to "abc123") {
    logger.info(LogSymbol.USER, "User did something")
}
```

This keeps your logs clean and avoids "leaking" context between threads or requests.

---

## 📜 Example Usage

```kotlin
val logger = EmojiLogger.get<MyClass>()

logger.info(LogSymbol.GREEN_TICK, "Everything is working!")
logger.warn(LogSymbol.WARNING_SIGN, "Something looks off.")

logger.withMdc("userId" to "abc123") {
    logger.debug(LogSymbol.USER, "User fetched")
}
```

DSL-style top-level logging:

```kotlin
emojiLog(LogSymbol.STARTUP) { "App started!" }
emojiLog(LogSymbol.DB_QUERY, level = LogLevel.WARN) { "Slow query" }
```

---

## 🌿 Spring Boot Setup
`

No manual wiring needed.

---

## 🔑 DSL Cheatsheet

```kotlin
emojiLog(LogSymbol.HANDLER) { "Handled request" }
emojiLog(LogSymbol.HANDLER, LogLevel.DEBUG) { "Detailed trace" }

LogSymbol.DB logFormat ("Saved {}" to arrayOf("user"))
LogSymbol.DB logFormatAt params(LogLevel.INFO, "Saved {} to {}", "item", "table")

withMdc("sessionId" to "abc123") {
    emojiLog(LogSymbol.AUTH) { "Authenticated" }
}
```

---

## 🎨 Emoji Reference

| Symbol         | Emoji | Use Case               |
|----------------|--------|------------------------|
| `GREEN_TICK`   | ✅     | Success                |
| `STOP_SIGN`    | 🛑     | Errors                 |
| `WARNING_SIGN` | ⚠️     | Warnings               |
| `DEBUG`        | 🔍     | Debug info             |
| `BUG`          | 🐛     | Problems or exceptions |
| `APP_STARTUP`  | 🌱     | Lifecycle events       |
| `SHUTDOWN`     | ☠️     | App shutdown           |
| `DB_QUERY`     | 📊     | Database reads         |
| `DB_WRITE`     | 📄     | Database writes        |
| `THREAD`       | 🧵     | Coroutines             |
| `USER`         | 👤     | User-related logs      |

---

## 🧱 Using from Java

Although emoji-logger is written in idiomatic Kotlin, it is fully usable from Java!

A Java wrapper utility `EmojiLoggerJava` is provided for convenience:

```java
Logger logger = EmojiLoggerJava.getLogger(MyClass.class);
EmojiLoggerJava.info(logger, LogSymbol.GREEN_TICK, "Job complete");
EmojiLoggerJava.infoWithMarker(logger, LogSymbol.DB_WRITE, "Row inserted");
```

Available methods:
- `getLogger(Class<?>)`
- `info|warn|debug|error(Logger, LogSymbol, String)`
- `infoWithMarker|warnWithMarker|debugWithMarker|errorWithMarker`

> Use `MDC.put(...)` and `MDC.clear()` manually in Java.

---

## 📍 Roadmap

- [ ] JSON log format support
- [ ] Coroutine MDC context propagation improvements
- [ ] SLF4J Marker customization DSL
- [ ] Auto MDC sync with Micrometer
- [ ] Kotlin Flow + log interception

---

## 🤝 Contributing

Contributions welcome! If you'd like to improve this library, add new emoji symbols, or enhance Spring Boot integration, feel free to open a PR.

---

## 📄 License

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at:
http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

Copyright 2025 Rob Deas

