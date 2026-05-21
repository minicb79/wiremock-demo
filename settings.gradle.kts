pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
    plugins {
        // Versions must match those used in build-logic so that programmatic plugin.apply() calls resolve correctly
        id("org.jetbrains.kotlin.jvm") version "2.3.0"
        id("org.jetbrains.kotlin.plugin.spring") version "2.3.0"
        id("org.springframework.boot") version "4.0.5"
        id("io.spring.dependency-management") version "1.1.7"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("../build-logic")

rootProject.name = "wiremock-demo"

include("inventory-service")
include("order-service")
