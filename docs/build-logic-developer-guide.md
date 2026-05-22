# Build-Logic Conventions Plugins: Developer & Integration Guide

This guide describes how to bootstrap, configure, and develop a project using the custom convention plugins provided by `build-logic`. It covers standard configurations, catalog requirements, extension settings, and the mechanics of auto-generated/injected telemetry and client source files.

---

## 1. Bootstrapping a New Project

To build a project using the `build-logic` convention plugins, you must link the plugins and expose a standard version catalog (`libs.versions.toml`).

### A. Linking the Plugin Build (`settings.gradle.kts`)
Include a composite build declaration pointing to the `build-logic` directory. This allows you to work on build logic and target applications concurrently.

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

// Enable composite build for build-logic plugins
includeBuild("../build-logic")

rootProject.name = "my-new-application"
include("my-service")
```

### B. Basic Submodule Build (`build.gradle.kts`)
Apply the desired plugins directly. Here is a typical template for a Spring Boot Kotlin microservice with OpenTelemetry logging and OpenAPI generation:

```kotlin
plugins {
    id("com.minicdesign.spring-service")
    id("com.minicdesign.api-generation")
    id("com.minicdesign.spring-otel-logging")
}

dependencies {
    // Service-specific libraries
    implementation(libs.jakartaValidationApi)
    implementation(libs.swaggerAnnotations)
}

apiGeneration {
    openApiBasePackage.set("com.minicdesign.myproject.generated")
}
```

---

## 2. Required Version Catalog (`libs.versions.toml`)

The convention plugins look up libraries from the target project's `libs` Version Catalog. The target project's `gradle/libs.versions.toml` **must** declare the following versions and libraries so the plugins can resolve their dependencies dynamically.

```toml
[versions]
springBoot = "4.0.5"
jakartaValidation = "3.1.1"
swaggerAnnotations = "2.2.30"
jacksonDatabindNullable = "0.2.6"

[libraries]
# 1. Spring Boot & Web Dependencies
spring-boot-starter-web = { group = "org.springframework.boot", name = "spring-boot-starter-web" }
spring-boot-starter-restclient = { group = "org.springframework.boot", name = "spring-boot-starter-restclient" }
spring-boot-starter-actuator = { group = "org.springframework.boot", name = "spring-boot-starter-actuator" }
spring-boot-starter-test = { group = "org.springframework.boot", name = "spring-boot-starter-test" }
spring-boot-starter-opentelemetry = { group = "org.springframework.boot", name = "spring-boot-starter-opentelemetry" }

# 2. Kotlin Runtime Libraries
kotlin-reflect = { group = "org.jetbrains.kotlin", name = "kotlin-reflect" }
jackson-module-kotlin = { group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin" }

# 3. Observability & Micrometer Registries
micrometer-registry-otlp = { group = "io.micrometer", name = "micrometer-registry-otlp" }
micrometer-tracing-bridge-otel = { group = "io.micrometer", name = "micrometer-tracing-bridge-otel" }

# 4. API Generation Compile-time Requirements
jakartaValidationApi = { group = "jakarta.validation", name = "jakarta.validation-api", version.ref = "jakartaValidation" }
swaggerAnnotations = { group = "io.swagger.core.v3", name = "swagger-annotations", version.ref = "swaggerAnnotations" }
jacksonDatabindNullable = { group = "org.openapitools", name = "jackson-databind-nullable", version.ref = "jacksonDatabindNullable" }
```

> [!NOTE]
> In addition to the catalog libraries, `com.minicdesign.otel-base` uses target projects' dependencies but falls back internally to `io.opentelemetry:opentelemetry-bom:1.41.0` and `io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.21.0-alpha` if the catalog entries `opentelemetry-bom` or `opentelemetry-logback-appender` are absent.

---

## 3. Configuration & Customization (Overriding Extensions)

Several plugins expose configuration blocks (Extensions) allowing developers to override default configurations.

### A. Java & Kotlin Conventions
Control the target JVM version and quality check thresholds.

```kotlin
javaConventions {
    javaVersion.set(25)              // Default: 25 (JVM version)
    coverageThreshold.set(0.90)       // Default: 0.90 (JaCoCo branch coverage verification limit)
}

kotlinConventions {
    kotlinVersion.set(25)            // Default: 25 (JVM target)
    coverageThreshold.set(0.90)       // Default: 0.90
}
```

### B. OpenAPI & WSDL Code Generation (`com.minicdesign.api-generation`)
Direct where generated classes should be written and which tool versions to execute.

```kotlin
apiGeneration {
    openApiBasePackage.set("com.minicdesign.generated.openapi") // Default package prefix for OpenAPI classes
    wsdlBasePackage.set("com.minicdesign.generated.wsdl")       // Default package prefix for WSDL soap classes
    openApiVersion.set("7.11.0")                                // Default OpenAPI Generator CLI version
    cxfVersion.set("4.0.5")                                     // Default Apache CXF WSDL2Java version
}
```

### C. Docker Compose Orchestration (`com.minicdesign.docker-compose`)
Point to specific executable locations.

```kotlin
dockerCompose {
    dockerPath.set("docker")             // Default: "docker"
    dockerComposePath.set("docker-compose") // Default: auto-detected, can override if using customized paths
}
```

### D. Wiremock Stub Servers (`com.minicdesign.wiremock`)
Control server ports and root directory.

```kotlin
wiremock {
    port.set(8080)                                    // Default: 8080
    rootDir.set(layout.buildDirectory.dir("merged"))  // Default: build/wiremock/merged (where submodule mappings are combined)
    serverUrl.set("http://localhost:8080")            // Default: http://localhost:${port}
    checkMissingOnly.set(false)                       // Default: false for localhost, true for remote hosts
}
```

#### Plugin Tasks:
- **`mergeWiremockSources`**: Recursively scans all submodules for directories named `wiremock/mappings` and `wiremock/__files`. It:
  - Generates deterministic, request-based UUIDs for stubs missing an explicit ID.
  - Checks for collisions (fails the build if different request mappings share the same ID, or if identical requests return different responses).
  - Merges identical duplicate mappings and prints a report.
- **`startWiremock`**: Starts a background or blocking local WireMock server on the configured `port`, loading mappings from `rootDir`.
- **`stopWiremock`**: Stops the background local WireMock server.
- **`publishWiremock`**: Publishes merged stubs from `rootDir` to the configured `serverUrl` via the WireMock Admin API:
  - If `checkMissingOnly` is `true` (default for remote servers), it performs a diff against deployed mappings and only uploads missing ones.
  - If `checkMissingOnly` is `false` (default for local servers), it posts all mappings, overwriting existing definitions.

#### CLI Overrides:
You can dynamically override publishing parameters via Gradle project properties:
```bash
./gradlew publishWiremock -Pwiremock.serverUrl=http://my-remote-wiremock:8080 -Pwiremock.checkMissingOnly=true
```

---

## 4. Injected Classes & Resources (How Auto-Configuration Works)

Certain plugins inject Java classes and resources dynamically during compilation. This removes boilerplate code and sets up OpenTelemetry.

### A. Logging & Telemetry Injection (`com.minicdesign.spring-otel-logging`)
The plugin registers custom source sets and generators. When you build the service, a Gradle task outputs configuration classes under `build/generated/sources/otel/java/com/minicdesign/otel/`:

```
build/generated/sources/otel/java/com/minicdesign/otel/
├── InstallOpenTelemetryAppender.java   # Wires Logback appender to Spring-managed OpenTelemetry bean
├── TraceIdFilter.java                  # Adds X-Trace-Id header containing the current trace ID to HTTP responses
├── ContextPropagationConfiguration.java # Wires TaskDecorator to forward trace context into async execution threads
├── OpenTelemetryConfiguration.java      # Configures JVM garbage collector, thread, memory, and CPU metrics
└── OtelAutoConfiguration.java          # Aggregator configuration class that @Imports all of the above
```

To ensure these classes are scanned and instantiated without developers having to manually configure `@ComponentScan` (which might miss `com.minicdesign.otel` since applications are under subpackages of `com.minicdesign.wiremockdemo`), the plugin automatically injects a Spring Boot Auto-Configuration metadata resource:

*   **Location**: `build/generated/sources/otel/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
*   **Contents**: `com.minicdesign.otel.OtelAutoConfiguration`

During startup, Spring Boot reads the `imports` file, instantiates `OtelAutoConfiguration`, and automatically registers all OpenTelemetry and filter beans.

### B. Default Logback Configuration Injection
The plugin outputs a default `logback-spring.xml` containing the OpenTelemetry logging appender:

*   **Location**: `build/generated/sources/otel/resources/logback-spring.xml`
*   **Default Definition**:
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
        <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>

        <appender name="OTEL" class="io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender">
        </appender>

        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="OTEL"/>
        </root>
    </configuration>
    ```

---

## 5. IDE Setup: Integrating Generated Code in IntelliJ IDEA

IntelliJ IDEA needs to know about directories where files are generated at compile time so that imports resolve correctly in the editor.

The `com.minicdesign.api-generation` and `com.minicdesign.spring-otel-logging` plugins automatically configure the **IDEA Gradle Plugin** settings in target projects. They register the generated directories as source folders:

```kotlin
project.plugins.withType(IdeaPlugin::class.java) {
    val ideaModel = project.extensions.getByType(IdeaModel::class.java)
    val generatedSources = project.layout.buildDirectory.dir("generated/sources/otel/java").get().asFile
    ideaModel.module.generatedSourceDirs.add(generatedSources)
    ideaModel.module.excludeDirs.remove(generatedSources)
}
```

This ensures that:
- IntelliJ highlights generated OpenAPI/WSDL models and interfaces as valid classes.
- Autoscan and autocomplete options for generated OpenTelemetry components work immediately.
- Source directories are correctly mapped in the project's `.iml` configurations.
