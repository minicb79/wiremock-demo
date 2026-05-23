package com.minicdesign.wiremockdemo.order.adapter.out.http;

import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort;
import com.minicdesign.wiremockdemo.order.inventoryapi.model.InventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyStore;

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
			@Value("${feature.security-https-enabled:false}") boolean httpsEnabled,
			ObjectProvider<WiremockDivertingInterceptor> wiremockDivertingInterceptorProvider) {

		String localInventoryBaseUrl = inventoryBaseUrl;
		String localWiremockBaseUrl = wiremockBaseUrl;

		if (httpsEnabled) {
			if (localInventoryBaseUrl.startsWith("http://")) {
				localInventoryBaseUrl = localInventoryBaseUrl.replace("http://", "https://");
			}
			if (localWiremockBaseUrl.startsWith("http://")) {
				localWiremockBaseUrl = localWiremockBaseUrl.replace("http://", "https://");
			}
		}

		HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
		if (httpsEnabled) {
			httpClientBuilder.sslContext(createSSLContext());
		}

		String finalBaseUrl;
		if (asProxyEnabled) {
			if (useForwardProxy) {
				URI proxyUri = URI.create(localWiremockBaseUrl);
				String host = proxyUri.getHost() != null ? proxyUri.getHost() : "localhost";
				int port = proxyUri.getPort() != -1
						? proxyUri.getPort()
						: ("https".equals(proxyUri.getScheme()) ? 443 : 80);

				log.info(
						"[InventoryHttpAdapter] Wiremock as Forward Proxy is ENABLED. Routing traffic through HTTP Proxy at {}:{}",
						host, port);
				httpClientBuilder.version(HttpClient.Version.HTTP_1_1)
						.proxy(ProxySelector.of(new InetSocketAddress(host, port)));
				HttpClient httpClient = httpClientBuilder.build();
				restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
				finalBaseUrl = localInventoryBaseUrl;
			} else {
				log.info("[InventoryHttpAdapter] Wiremock as Reverse Proxy is ENABLED. Routing traffic to WireMock: {}",
						localWiremockBaseUrl);
				if (httpsEnabled) {
					HttpClient httpClient = httpClientBuilder.build();
					restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
				}
				finalBaseUrl = localWiremockBaseUrl;
			}
		} else {
			if (httpsEnabled) {
				HttpClient httpClient = httpClientBuilder.build();
				restClientBuilder.requestFactory(new JdkClientHttpRequestFactory(httpClient));
			}
			finalBaseUrl = localInventoryBaseUrl;
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

	private SSLContext createSSLContext() {
		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			try (InputStream is = new ClassPathResource("certificates/keystore.p12").getInputStream()) {
				keyStore.load(is, "changeit".toCharArray());
			}

			KeyStore trustStore = KeyStore.getInstance("PKCS12");
			try (InputStream is = new ClassPathResource("certificates/truststore.p12").getInputStream()) {
				trustStore.load(is, "changeit".toCharArray());
			}

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(keyStore, "changeit".toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			return sslContext;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to configure SSL Context", e);
		}
	}
}
