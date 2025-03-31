plugins {
    kotlin("jvm") version "2.1.10"

    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "tech.robd"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("org.slf4j:slf4j-api:2.0.9")

    // Testing (optional but recommended)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

   // implementation(project(":core"))

    // Spring Boot Auto-config
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework.boot:spring-boot-configuration-processor")

    // Required to make @ConfigurationProperties work
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Servlet API for Filter
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")

    // Web conditional support (optional)
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.mockito:mockito-core:5.10.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    // Spring test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-test")

    // Spring MockMvc dependencies
    testImplementation("org.springframework.security:spring-security-test")

    // For Kotlin-specific Spring test utilities
    testImplementation("com.ninja-squad:springmockk:4.0.2")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}