package com.minicdesign.wiremockdemo.order.adapter.out.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

@Configuration
@Profile("!prod")
@ConditionalOnProperty(name = "feature.security-https-enabled", havingValue = "false", matchIfMissing = true)
public class HttpClientConfig {

	@Bean
	public HttpClient wiremockHttpClient() {
		return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	}

	@Bean
	public RestClient wiremockRestClient(HttpClient wiremockHttpClient,
			@Value("${services.wiremock.base-url:http://localhost:8092}") String wiremockBaseUrl) {
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(wiremockHttpClient);
		return RestClient.builder().baseUrl(wiremockBaseUrl).requestFactory(requestFactory).build();
	}
}
