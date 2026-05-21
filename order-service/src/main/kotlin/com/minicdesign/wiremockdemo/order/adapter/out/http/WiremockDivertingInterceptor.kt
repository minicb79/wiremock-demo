package com.minicdesign.wiremockdemo.order.adapter.out.http

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.io.ByteArrayInputStream
import java.io.InputStream

@Component
class WiremockDivertingInterceptor(
    private val wiremockRestClient: RestClient,
    @param:Value("\${feature.wiremock-via-interceptor:false}") private val featureEnabled: Boolean,
) : ClientHttpRequestInterceptor {
    private val logger = LoggerFactory.getLogger(WiremockDivertingInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val uri = request.uri

        // Divert calls when feature flag is enabled and productId is PROD-800
        if (featureEnabled && uri.path.contains("PROD-800")) {
            logger.info("Diverting call to WireMock for URI: {} since productId is PROD-800", uri)
            val pathWithQuery = uri.path + (uri.query?.let { "?$it" } ?: "")

            val responseEntity =
                wiremockRestClient
                    .method(request.method)
                    .uri(pathWithQuery)
                    .headers { headers ->
                        headers.putAll(request.headers)
                    }.body(body)
                    .retrieve()
                    .toEntity(ByteArray::class.java)

            return object : ClientHttpResponse {
                override fun getStatusCode(): HttpStatusCode = responseEntity.statusCode

                override fun getStatusText(): String = ""

                override fun getHeaders(): HttpHeaders = responseEntity.headers

                override fun getBody(): InputStream = ByteArrayInputStream(responseEntity.body ?: ByteArray(0))

                override fun close() {}
            }
        }

        return execution.execute(request, body)
    }
}
