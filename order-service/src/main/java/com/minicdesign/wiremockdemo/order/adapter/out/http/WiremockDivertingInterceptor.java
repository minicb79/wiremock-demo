package com.minicdesign.wiremockdemo.order.adapter.out.http;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

@Component
@Profile("!prod")
@Slf4j
public class WiremockDivertingInterceptor implements ClientHttpRequestInterceptor {
    private final RestClient wiremockRestClient;
    private final boolean featureEnabled;
    private final String wiremockBaseUrl;

    public WiremockDivertingInterceptor(
            RestClient wiremockRestClient,
            @Value("${feature.wiremock-via-interceptor:false}") boolean featureEnabled,
            @Value("${services.wiremock.base-url}") String wiremockBaseUrl
    ) {
        this.wiremockRestClient = wiremockRestClient;
        this.featureEnabled = featureEnabled;
        this.wiremockBaseUrl = wiremockBaseUrl;
    }

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution
    ) throws IOException {
        URI uri = request.getURI();

        log.info("========================================= WIREMOCK INTERCEPTOR =========================================");
        log.info("[WireMock Interceptor] Intercepting request: {} {}", request.getMethod(), uri);

        if (featureEnabled && uri.getPath().contains("PROD-800")) {
            String pathWithQuery = uri.getPath() + (uri.getQuery() != null ? "?" + uri.getQuery() : "");
            String targetUri = wiremockBaseUrl + pathWithQuery;
            log.info("[WireMock Interceptor] MATCH DETECTED for product 'PROD-800'.");
            log.info("[WireMock Interceptor] DIVERTING request to WireMock: {} -> {}", uri, targetUri);

            try {
                ResponseEntity<byte[]> responseEntity = wiremockRestClient
                        .method(request.getMethod())
                        .uri(pathWithQuery)
                        .headers(headers -> headers.putAll(request.getHeaders()))
                        .body(body)
                        .retrieve()
                        .toEntity(byte[].class);

                log.info("[WireMock Interceptor] SUCCESS: Received response from WireMock: {}", responseEntity.getStatusCode());
                log.info("=========================================================================================================");

                return new ClientHttpResponse() {
                    @Override
                    public HttpStatusCode getStatusCode() {
                        return responseEntity.getStatusCode();
                    }

                    @Override
                    public String getStatusText() {
                        return "";
                    }

                    @Override
                    public HttpHeaders getHeaders() {
                        return responseEntity.getHeaders();
                    }

                    @Override
                    public InputStream getBody() {
                        byte[] respBody = responseEntity.getBody();
                        return new ByteArrayInputStream(respBody != null ? respBody : new byte[0]);
                    }

                    @Override
                    public void close() {
                    }
                };
            } catch (Exception ex) {
                log.error("[WireMock Interceptor] ERROR during diversion to WireMock: {}", ex.getMessage(), ex);
                log.info("=========================================================================================================");
                throw ex;
            }
        }

        log.info("[WireMock Interceptor] NO MATCH for product 'PROD-800'. Proceeding with standard execution.");
        log.info("=========================================================================================================");
        return execution.execute(request, body);
    }
}
