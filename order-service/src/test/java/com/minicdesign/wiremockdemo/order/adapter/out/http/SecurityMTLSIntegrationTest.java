package com.minicdesign.wiremockdemo.order.adapter.out.http;

import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, properties = {"server.port=8444",
		"server.ssl.enabled=true", "server.ssl.client-auth=need",
		"server.ssl.key-store=classpath:certificates/keystore.p12", "server.ssl.key-store-password=changeit",
		"server.ssl.key-store-type=PKCS12", "server.ssl.trust-store=classpath:certificates/truststore.p12",
		"server.ssl.trust-store-password=changeit", "server.ssl.trust-store-type=PKCS12",
		"feature.security-https-enabled=true", "services.inventory.base-url=https://localhost:8444"})
public class SecurityMTLSIntegrationTest {

	@Autowired
	private InventoryPort inventoryPort;

	@Test
	public void shouldPerformSuccessfulMtlsHandshake() {
		// Act
		Integer quantity = inventoryPort.getAvailableQuantity("PROD-777");

		// Assert
		assertNotNull(quantity);
		assertEquals(15, quantity);
	}

	@Test
	public void shouldFailHandshakeWhenNoClientCertificatesPresented() {
		// Create a plain RestClient without keystore/truststore configured
		RestClient plainClient = RestClient.builder().build();
		String url = "https://localhost:8444/v1/inventory/PROD-777";

		// Direct call should fail due to SSL/Handshake error because server demands
		// client certs
		assertThrows(Exception.class, () -> {
			plainClient.get().uri(url).retrieve().toBodilessEntity();
		});
	}

	@TestConfiguration
	static class TestConfig {
		@Bean
		public TestInventoryController testInventoryController() {
			return new TestInventoryController();
		}
	}

	@RestController
	static class TestInventoryController {
		@GetMapping("/v1/inventory/{productId}")
		public ResponseEntity<Map<String, Object>> getInventory(@PathVariable String productId) {
			return ResponseEntity.ok(Map.of("productId", productId, "quantity", 15, "available", true));
		}
	}
}
