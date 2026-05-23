package com.minicdesign.wiremockdemo.order.adapter.out.http;

import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort;
import com.minicdesign.wiremockdemo.order.inventoryapi.model.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;

@Component
@Slf4j
public class InventoryHttpAdapter implements InventoryPort {
	private final RestClient restClient;

	public InventoryHttpAdapter(RestClient.Builder restClientBuilder,
			@Value("${services.inventory.base-url}") String inventoryBaseUrl,
			@Value("${services.wiremock.base-url:}") String wiremockBaseUrl,
			@Value("${feature.wiremock-as-proxy:false}") boolean asProxyEnabled,
			@Value("${feature.wiremock-via-interceptor:false}") boolean viaInterceptorEnabled,
			@Value("${feature.wiremock-use-forward-proxy:false}") boolean useForwardProxy,
			ObjectProvider<WiremockDivertingInterceptor> wiremockDivertingInterceptorProvider) {
		String finalBaseUrl;
		if (asProxyEnabled) {
			if (useForwardProxy) {
				URI proxyUri = URI.create(wiremockBaseUrl);
				String host = proxyUri.getHost() != null ? proxyUri.getHost() : "localhost";
				int port = proxyUri.getPort() != -1
						? proxyUri.getPort()
						: ("https".equals(proxyUri.getScheme()) ? 443 : 80);

				log.info(
						"[InventoryHttpAdapter] Wiremock as Forward Proxy is ENABLED. Routing traffic through HTTP Proxy at {}:{}",
						host, port);
				HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
						.proxy(ProxySelector.of(new InetSocketAddress(host, port))).build();
				restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
				finalBaseUrl = inventoryBaseUrl;
			} else {
				log.info("[InventoryHttpAdapter] Wiremock as Reverse Proxy is ENABLED. Routing traffic to WireMock: {}",
						wiremockBaseUrl);
				finalBaseUrl = wiremockBaseUrl;
			}
		} else {
			finalBaseUrl = inventoryBaseUrl;
		}

		if (!asProxyEnabled && viaInterceptorEnabled) {
			wiremockDivertingInterceptorProvider.ifAvailable(interceptor -> {
				log.info("[InventoryHttpAdapter] WireMock Diverting Interceptor is ENABLED. Attaching interceptor.");
				restClientBuilder.requestInterceptor(interceptor);
			});
		}

		this.restClient = restClientBuilder.baseUrl(finalBaseUrl).build();
	}

	@Override
	public Integer getAvailableQuantity(String productId) {
		log.info("Calling inventory-service for productId={}", productId);
		try {
			InventoryResponse response = restClient.get().uri("/v1/inventory/{productId}", productId).retrieve()
					.body(InventoryResponse.class);

			if (response == null) {
				return null;
			}

			Integer quantity = response.getQuantity();
			log.info("Inventory response for productId={} quantity={} available={}", productId, quantity,
					response.getAvailable());

			return Boolean.TRUE.equals(response.getAvailable()) ? quantity : 0;
		} catch (HttpClientErrorException.NotFound ex) {
			log.warn("Product not found in inventory-service: productId={}", productId);
			return null;
		} catch (Exception ex) {
			log.error("Error calling inventory-service for productId={}: {}", productId, ex.getMessage());
			throw ex;
		}
	}
}
