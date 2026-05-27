plugins {
    id("com.minicdesign.docker-compose")
    id("com.minicdesign.wiremock")
}

wiremock {
    port.set(8092)
}

dockerCompose {
    dockerComposePath.set("/opt/homebrew/bin/docker-compose")
    dockerPath.set("/opt/homebrew/bin/docker")
}

tasks.register("downloadOtelJavaagent") {
    val destFile = file("docker/wiremock/libs/opentelemetry-javaagent.jar")
    outputs.file(destFile)
    doLast {
        if (!destFile.exists()) {
            destFile.parentFile.mkdirs()
            println("Downloading OpenTelemetry Javaagent...")
            val url = java.net.URI("https://repo1.maven.org/maven2/io/opentelemetry/javaagent/opentelemetry-javaagent/1.32.0/opentelemetry-javaagent-1.32.0.jar").toURL()
            url.openStream().use { inputStream ->
                destFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            println("OpenTelemetry Javaagent downloaded successfully.")
        }
    }
}

tasks.register("buildStdoutRedirectAgent") {
    val srcFile = file("docker/wiremock/agent/src/com/minicdesign/otel/StdoutRedirectAgent.java")
    val destFile = file("docker/wiremock/libs/stdout-redirect-agent.jar")
    inputs.file(srcFile)
    outputs.file(destFile)

    doLast {
        val buildDir = file("docker/wiremock/agent/build")
        buildDir.deleteRecursively()
        val classesDir = buildDir.resolve("classes")
        classesDir.mkdirs()

        // 1. Compile Java source
        val compiler = javax.tools.ToolProvider.getSystemJavaCompiler()
            ?: throw GradleException("System Java compiler not found. Ensure you are running Gradle with a JDK.")

        val result = compiler.run(null, null, null, "--release", "17", "-d", classesDir.absolutePath, srcFile.absolutePath)
        if (result != 0) {
            throw GradleException("Failed to compile StdoutRedirectAgent.java")
        }

        // 2. Package JAR
        destFile.parentFile.mkdirs()
        java.util.jar.JarOutputStream(
            destFile.outputStream(),
            java.util.jar.Manifest().apply {
                mainAttributes[java.util.jar.Attributes.Name.MANIFEST_VERSION] = "1.0"
                mainAttributes[java.util.jar.Attributes.Name("Premain-Class")] = "com.minicdesign.otel.StdoutRedirectAgent"
            }
        ).use { jarOutputStream ->
            classesDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(classesDir).path.replace('\\', '/')
                    val entry = java.util.jar.JarEntry(relativePath)
                    jarOutputStream.putNextEntry(entry)
                    file.inputStream().use { it.copyTo(jarOutputStream) }
                    jarOutputStream.closeEntry()
                }
            }
        }
        println("Stdout redirect agent compiled and packaged successfully.")
    }
}

tasks.named("startWiremock") {
    dependsOn("downloadOtelJavaagent", "buildStdoutRedirectAgent", "mergeWiremockSources")
}

tasks.register<Exec>("generateCertificates") {
    group = "verification"
    description = "Generates self-signed keystore and truststore files for local HTTPS testing."
    commandLine("bash", "scripts/generate-certs.sh")
}

