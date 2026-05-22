plugins {
    id("com.minicdesign.spring-service")
    id("com.minicdesign.api-generation")
    id("com.minicdesign.spring-otel-logging")
    id("com.minicdesign.pact")
}

dependencies {
    implementation(libs.jakartaValidationApi)
    implementation(libs.swaggerAnnotations)
    implementation(libs.jacksonDatabindNullable)
    testImplementation(libs.pact.consumer)
}

apiGeneration {
    openApiBasePackage.set("com.minicdesign.wiremockdemo.order")
}

configure<com.minicdesign.buildlogic.JavaConventionsExtension> {
    coverageThreshold.set(0.0)
}

sourceSets {
    create("testPactConsumerInventoryService")
}
