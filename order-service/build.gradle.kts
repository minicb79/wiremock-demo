plugins {
    id("com.minicdesign.spring-service")
    id("com.minicdesign.api-generation")
    id("com.minicdesign.spring-otel-logging")
}

dependencies {
    implementation(libs.jakartaValidationApi)
    implementation(libs.swaggerAnnotations)
    implementation(libs.jacksonDatabindNullable)
}

apiGeneration {
    openApiBasePackage.set("com.minicdesign.wiremockdemo.order")
}
