package com.minicdesign.wiremockdemo.order.adapter.out.http;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class InventoryHttpAdapterTest {

	@Test
	public void shouldConfigurePlainHttpWhenHttpsDisabled() {
		RestClient.Builder builder = mock(RestClient.Builder.class);
		RestClient restClient = mock(RestClient.class);
		when(builder.baseUrl(any(String.class))).thenReturn(builder);
		when(builder.build()).thenReturn(restClient);

		ObjectProvider<WiremockDivertingInterceptor> interceptorProvider = mock(ObjectProvider.class);

		new InventoryHttpAdapter(builder, "http://localhost:8081", "http://localhost:8092", false, // asProxyEnabled
				false, // viaInterceptorEnabled
				false, // useForwardProxy
				false, // httpsEnabled
				interceptorProvider);

		verify(builder).baseUrl("http://localhost:8081");
		verify(builder, never()).requestFactory(any(ClientHttpRequestFactory.class));
	}

	@Test
	public void shouldConfigureHttpsAndLoadSslContextWhenHttpsEnabled() {
		RestClient.Builder builder = mock(RestClient.Builder.class);
		RestClient restClient = mock(RestClient.class);
		when(builder.baseUrl(any(String.class))).thenReturn(builder);
		when(builder.build()).thenReturn(restClient);

		ObjectProvider<WiremockDivertingInterceptor> interceptorProvider = mock(ObjectProvider.class);

		new InventoryHttpAdapter(builder, "http://localhost:8081", "http://localhost:8092", false, // asProxyEnabled
				false, // viaInterceptorEnabled
				false, // useForwardProxy
				true, // httpsEnabled
				interceptorProvider);

		verify(builder).baseUrl("https://localhost:8081");

		ArgumentCaptor<ClientHttpRequestFactory> factoryCaptor = ArgumentCaptor
				.forClass(ClientHttpRequestFactory.class);
		verify(builder).requestFactory(factoryCaptor.capture());

		assertTrue(factoryCaptor.getValue() instanceof JdkClientHttpRequestFactory);
	}

	@Test
	public void shouldConfigureHttpsWithWiremockAsReverseProxy() {
		RestClient.Builder builder = mock(RestClient.Builder.class);
		RestClient restClient = mock(RestClient.class);
		when(builder.baseUrl(any(String.class))).thenReturn(builder);
		when(builder.build()).thenReturn(restClient);

		ObjectProvider<WiremockDivertingInterceptor> interceptorProvider = mock(ObjectProvider.class);

		new InventoryHttpAdapter(builder, "http://localhost:8081", "http://localhost:8092", true, // asProxyEnabled
				false, // viaInterceptorEnabled
				false, // useForwardProxy (reverse proxy mode)
				true, // httpsEnabled
				interceptorProvider);

		// In reverse proxy, it should point to WireMock URL converted to HTTPS
		verify(builder).baseUrl("https://localhost:8092");

		ArgumentCaptor<ClientHttpRequestFactory> factoryCaptor = ArgumentCaptor
				.forClass(ClientHttpRequestFactory.class);
		verify(builder).requestFactory(factoryCaptor.capture());
		assertTrue(factoryCaptor.getValue() instanceof JdkClientHttpRequestFactory);
	}

	@Test
	public void shouldConfigureHttpsWithWiremockAsForwardProxy() {
		RestClient.Builder builder = mock(RestClient.Builder.class);
		RestClient restClient = mock(RestClient.class);
		when(builder.baseUrl(any(String.class))).thenReturn(builder);
		when(builder.build()).thenReturn(restClient);

		ObjectProvider<WiremockDivertingInterceptor> interceptorProvider = mock(ObjectProvider.class);

		new InventoryHttpAdapter(builder, "http://localhost:8081", "http://localhost:8092", true, // asProxyEnabled
				false, // viaInterceptorEnabled
				true, // useForwardProxy (forward proxy mode)
				true, // httpsEnabled
				interceptorProvider);

		// In forward proxy, final base URL is the inventory-service itself (HTTPS)
		verify(builder).baseUrl("https://localhost:8081");

		ArgumentCaptor<ClientHttpRequestFactory> factoryCaptor = ArgumentCaptor
				.forClass(ClientHttpRequestFactory.class);
		verify(builder).requestFactory(factoryCaptor.capture());
		assertTrue(factoryCaptor.getValue() instanceof JdkClientHttpRequestFactory);
	}
}
