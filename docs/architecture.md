# 🏗️ emoji-logger Architecture Overview

This document provides a high-level overview of how the `emoji-logger` library is structured, initialized, and wired. The goal is to help developers quickly understand how it works internally and how to extend or contribute to it.

---

## 🧹 Core Concepts

### EmojiLoggerImpl
A Kotlin class that implements `Logger` and enhances it with emoji-based structured logging and dynamic caller resolution. Supports:

- Emoji log methods (e.g. `info(LogSymbol, message)`, `errorWithMarker(...)`, etc.)
- MDC-aware logging (`withMdc`, `withMdcSuspend`)
- SLF4J compatibility (delegates all standard `Logger` methods)
- Dynamic fallback to `CallerResolver` or static `Logger` via `forClass(...)`

### EmojiLogger (Object)
Factory-style utility that provides `EmojiLoggerImpl` instances based on class or name. Ideal for DSL use or Spring Boot. Designed to be concise and discoverable in Kotlin.

```kotlin
val logger = EmojiLogger.get<MyService>()
```

> The separation between `EmojiLogger` and `EmojiLoggerImpl` provides:
> - Kotlin-friendly DSL and static `get()` resolution from `EmojiLogger`
> - SLF4J `Logger` implementation in `EmojiLoggerImpl` for compatibility and structured usage

This split ensures idiomatic Kotlin while remaining Java-compatible for integration.

### LogSymbol
A structured Kotlin `enum class` mapping symbolic names (e.g. `DB_QUERY`, `APP_STARTUP`) to emoji and SLF4J marker names.

---

## 💡 Logging DSL

The `emoji-logger` DSL provides expressive top-level log functions with automatic emoji formatting:

```kotlin
emojiLog(LogSymbol.STARTUP) { "Boot complete" }
emojiLog(LogSymbol.BUG, LogLevel.ERROR) { "Unexpected error" }
```

Supports infix extensions:

```kotlin
LOG_HANDLER log "Handling request"
LOG_DB logFormat ("Saved {}" to arrayOf("user"))
LOG_HANDLER logFormatAt params(LogLevel.INFO, "Handled {}", "user")
```

---

## ⚙️ MDC Infrastructure

### Features
- Uses SLF4J MDC (Mapped Diagnostic Context)
- Fully thread-safe and coroutine-aware
- Automatically restores MDC state after logging blocks

### Example
```kotlin
emojiLogger.withMdc("userId" to "abc") {
    emojiLogger.info(LogSymbol.USER, "User login")
}
```

---

## 🌿 Spring Boot Integration

If `emoji-logger-starter` is included in the project, the following auto-configured beans are registered:

- `EarlyMdcInitializer` (early context setup)
- `FullMdcInitializer` (post-startup enrichment)
- `EmojiMdcWebFilter` (per-request MDC setup for HTTP requests)

These register MDC entries such as:
- `initPhase`, `pid`, `profile`, `app`
- `method`, `path`, `remoteAddr`, `requestId`

Auto-clears after each request using servlet filter lifecycle.

---

## 🔍 Caller Resolution

### CallerResolver
- Extracts the caller class from `Thread.currentThread().stackTrace`
- Skips internal Kotlin, coroutine, and SLF4J frames
- Filters out `CallerResolver` itself for accuracy
- Globally toggleable with `CallerResolver.enableCallerDetection`

### CallerResolverFactory
- Provides a default/global resolver
- Testable/mutable for mocking `getLoggerForCaller()`

---

## 🔒 Security & Safety

- MDC is always cleared and restored safely
- Spring Web MDC is per-request scoped
- SLF4J markers prevent collision between emoji logs and raw logs
- License: Apache 2.0 (flexible, non-viral, permissive)

---

## 🪪 Java Interoperability & Design Considerations

### Java Compatibility
While `emoji-logger` is written in Kotlin, it supports Java usage via:
- Full SLF4J `Logger` implementation (`EmojiLoggerImpl`)
- `@JvmStatic` entry points in the companion object of `EmojiLoggerImpl`

Java example:
```java
Logger logger = EmojiLoggerImpl.forClass(MyService.class);
logger.info(LogSymbol.GREEN_TICK.getEmoji() + " Success!");
```

For emoji-enhanced methods with markers:
```java
EmojiLoggerImpl.forClass(MyService.class)
    .infoWithMarker(LogSymbol.DB_WRITE, "Insert complete");
```

Java code can also use standard SLF4J APIs on `EmojiLoggerImpl` since it implements the `Logger` interface. MDC must be managed manually via `MDC.put(...)` in Java.

### Design Rationale
- `EmojiLoggerImpl` implements `Logger` to integrate directly with Java SLF4J infrastructure
- `EmojiLogger` is idiomatic Kotlin: provides DSL-friendly creation and dynamic resolution
- `EmojiLoggerImpl` exposes Java-friendly static factory methods like `forClass()` and `forName()` via `@JvmStatic`
- The split also allows tests, filters, and core API to depend only on interfaces when needed
- Logger resolution is dynamic by default for flexibility, but `forClass(...)` and `forName(...)` allow binding for hot paths

---

## 🧺 Testing Setup

- Uses JUnit 5
- Logback `ListAppender` for capturing logs
- Mockito used to verify `CallerResolver` and logger interactions
- Tests cover:
  - DSL-style logging
  - MDC and suspend blocks
  - Caller resolution logic
  - Fallback cases and overrides

---

## 🔧 Extending emoji-logger

You can easily:
- Add new `LogSymbol` values with emoji + marker
- Create your own MDC injectors (Spring filter, coroutine interceptors)
- Replace the `CallerResolver` for special stack scenarios

The architecture is modular, composable, and uses Kotlin idioms.

---

Questions? Ideas? PRs? Come contribute or open an issue!

