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
    testImplementation(libs.pact.provider)
}

apiGeneration {
    openApiBasePackage.set("com.minicdesign.wiremockdemo.inventory")
}

sourceSets {
    create("testPactProvider")
}
