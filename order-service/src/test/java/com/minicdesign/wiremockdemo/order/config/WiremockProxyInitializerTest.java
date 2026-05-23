package com.minicdesign.wiremockdemo.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WiremockProxyInitializerTest {

	@Test
	public void shouldNotRegisterProxyMappingWhenFeatureIsDisabled() {
		RestClient restClient = mock(RestClient.class);
		ObjectMapper objectMapper = new ObjectMapper();
		WiremockProxyInitializer initializer = new WiremockProxyInitializer(restClient, objectMapper, false,
				"http://localhost:8081");

		initializer.run();

		verify(restClient, never()).post();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldRegisterProxyMappingWhenFeatureIsEnabled() throws IOException {
		RestClient restClient = mock(RestClient.class);
		RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
		RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
		RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
		ObjectMapper objectMapper = new ObjectMapper();

		ResponseEntity<Void> responseEntity = mock(ResponseEntity.class);

		when(restClient.post()).thenReturn(postSpec);
		when(postSpec.uri(any(String.class))).thenReturn(postSpec);
		when(postSpec.contentType(any())).thenReturn(postSpec);
		when(postSpec.body(any())).thenReturn(bodySpec);
		when(bodySpec.retrieve()).thenReturn(responseSpec);
		when(responseSpec.toBodilessEntity()).thenReturn(responseEntity);
		when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);

		WiremockProxyInitializer initializer = new WiremockProxyInitializer(restClient, objectMapper, true,
				"http://localhost:8081");

		initializer.run();

		verify(restClient).post();
		verify(postSpec).uri("/__admin/mappings");

		ArgumentCaptor<Object> bodyCaptor = ArgumentCaptor.forClass(Object.class);
		verify(postSpec).body(bodyCaptor.capture());
		Map<?, ?> bodyMap = (Map<?, ?>) bodyCaptor.getValue();
		Map<?, ?> responseMap = (Map<?, ?>) bodyMap.get("response");
		assertEquals("http://host.docker.internal:8081", responseMap.get("proxyBaseUrl"));

		Map<String, String> expectedRequestCriteria = Map.of("method", "ANY", "urlPattern", "/v1/inventory/.*");
		byte[] expectedBytes = objectMapper.writeValueAsBytes(expectedRequestCriteria);
		String expectedUuid = UUID.nameUUIDFromBytes(expectedBytes).toString();
		assertEquals(expectedUuid, bodyMap.get("id"));
	}

	@Test
	public void shouldLogAndNotThrowExceptionWhenRegistrationFails() {
		RestClient restClient = mock(RestClient.class);
		when(restClient.post()).thenThrow(new RuntimeException("Connection refused"));
		ObjectMapper objectMapper = new ObjectMapper();

		WiremockProxyInitializer initializer = new WiremockProxyInitializer(restClient, objectMapper, true,
				"http://localhost:8081");

		assertDoesNotThrow(() -> initializer.run());
	}
}
