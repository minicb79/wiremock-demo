package com.minicdesign.wiremockdemo.order.adapter.out.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import org.springframework.core.io.ClassPathResource;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.security.KeyStore;

@Configuration
@Profile("!prod")
@ConditionalOnProperty(name = "feature.security-https-enabled", havingValue = "true")
public class TlsHttpClientConfig {

	@Bean
	public HttpClient wiremockHttpClient() {
		return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).sslContext(createSSLContext()).build();
	}

	@Bean
	public RestClient wiremockRestClient(HttpClient wiremockHttpClient,
			@Value("${services.wiremock.base-url:https://localhost:8443}") String wiremockBaseUrl) {
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(wiremockHttpClient);
		return RestClient.builder().baseUrl(wiremockBaseUrl).requestFactory(requestFactory).build();
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
			throw new IllegalStateException("Failed to configure SSL Context for WireMock client", e);
		}
	}
}
