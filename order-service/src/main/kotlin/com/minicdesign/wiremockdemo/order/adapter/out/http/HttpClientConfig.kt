package com.minicdesign.wiremockdemo.order.adapter.out.http

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient

@Configuration
class HttpClientConfig {
    @Bean
    fun wiremockHttpClient(): HttpClient = HttpClient.newBuilder().build()

    @Bean
    fun wiremockRestClient(
        wiremockHttpClient: HttpClient,
        @Value("\${services.wiremock.base-url:http://localhost:8092}") wiremockBaseUrl: String,
    ): RestClient {
        val requestFactory = JdkClientHttpRequestFactory(wiremockHttpClient)
        return RestClient
            .builder()
            .baseUrl(wiremockBaseUrl)
            .requestFactory(requestFactory)
            .build()
    }
}
