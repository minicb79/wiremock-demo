package com.minicdesign.wiremockdemo.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@Profile("local")
@Slf4j
public class WiremockProxyInitializer implements CommandLineRunner {
	private final RestClient wiremockRestClient;
	private final ObjectMapper objectMapper;
	private final boolean asProxyEnabled;
	private final String inventoryBaseUrl;

	public WiremockProxyInitializer(RestClient wiremockRestClient, ObjectMapper objectMapper,
			@Value("${feature.wiremock-as-proxy:false}") boolean asProxyEnabled,
			@Value("${services.inventory.base-url}") String inventoryBaseUrl) {
		this.wiremockRestClient = wiremockRestClient;
		this.objectMapper = objectMapper;
		this.asProxyEnabled = asProxyEnabled;
		this.inventoryBaseUrl = inventoryBaseUrl;
	}

	@Override
	public void run(@Nullable String... args) {
		if (!asProxyEnabled) {
			log.info("[WiremockProxyInitializer] Wiremock as Proxy is disabled. Skipping proxy mapping registration.");
			return;
		}

		log.info("[WiremockProxyInitializer] Wiremock as Proxy is ENABLED. Registering catch-all proxy mapping...");

		String targetBaseUrl = inventoryBaseUrl.replace("localhost", "host.docker.internal").replace("127.0.0.1",
				"host.docker.internal");

		Map<String, String> requestCriteria = Map.of("method", "ANY", "urlPattern", "/v1/inventory/.*");

		try {
			byte[] requestBytes = objectMapper.writeValueAsBytes(requestCriteria);
			String mappingId = UUID.nameUUIDFromBytes(requestBytes).toString();

			Map<String, Object> mappingBody = Map.of("id", mappingId, "request", requestCriteria, "response",
					Map.of("proxyBaseUrl", targetBaseUrl), "priority", 10);

			var response = wiremockRestClient.post().uri("/__admin/mappings").contentType(MediaType.APPLICATION_JSON)
					.body(mappingBody).retrieve().toBodilessEntity();

			if (response.getStatusCode().is2xxSuccessful()) {
				log.info("[WiremockProxyInitializer] SUCCESS: Registered catch-all proxy mapping to: {}",
						targetBaseUrl);
			} else {
				log.warn("[WiremockProxyInitializer] WARNING: Failed to register catch-all proxy. WireMock status: {}",
						response.getStatusCode());
			}
		} catch (Exception ex) {
			log.error(
					"[WiremockProxyInitializer] ERROR: Failed to register catch-all proxy mapping. Ensure WireMock is running. Error details: {}",
					ex.getMessage(), ex);
		}
	}
}
