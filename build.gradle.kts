plugins {
    id("com.minicdesign.docker-compose")
    id("com.minicdesign.wiremock")
}

wiremock {
    port.set(8092)
}
