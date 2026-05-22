package com.minicdesign.wiremockdemo.order.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
@Profile("local")
class WiremockProxyInitializer(
    private val wiremockRestClient: RestClient,
    @param:Value("\${feature.wiremock-as-proxy:false}") private val asProxyEnabled: Boolean,
    @param:Value("\${services.inventory.base-url}") private val inventoryBaseUrl: String,
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(WiremockProxyInitializer::class.java)

    override fun run(vararg args: String) {
        if (!asProxyEnabled) {
            logger.info("[WiremockProxyInitializer] Wiremock as Proxy is disabled. Skipping proxy mapping registration.")
            return
        }

        logger.info("[WiremockProxyInitializer] Wiremock as Proxy is ENABLED. Registering catch-all proxy mapping...")

        val targetBaseUrl =
            inventoryBaseUrl
                .replace("localhost", "host.docker.internal")
                .replace("127.0.0.1", "host.docker.internal")

        val mappingBody =
            mapOf(
                "request" to
                    mapOf(
                        "method" to "ANY",
                        "urlPattern" to "/inventory/.*",
                    ),
                "response" to
                    mapOf(
                        "proxyBaseUrl" to targetBaseUrl,
                    ),
                "priority" to 10,
            )

        try {
            val response =
                wiremockRestClient
                    .post()
                    .uri("/__admin/mappings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mappingBody)
                    .retrieve()
                    .toBodilessEntity()

            if (response.statusCode.is2xxSuccessful) {
                logger.info(
                    "[WiremockProxyInitializer] SUCCESS: Registered catch-all proxy mapping to: {}",
                    targetBaseUrl,
                )
            } else {
                logger.warn(
                    "[WiremockProxyInitializer] WARNING: Failed to register catch-all proxy. WireMock status: {}",
                    response.statusCode,
                )
            }
        } catch (ex: Exception) {
            logger.error(
                "[WiremockProxyInitializer] ERROR: Failed to register catch-all proxy mapping. " +
                    "Ensure WireMock is running. Error details: {}",
                ex.message,
                ex,
            )
        }
    }
}
