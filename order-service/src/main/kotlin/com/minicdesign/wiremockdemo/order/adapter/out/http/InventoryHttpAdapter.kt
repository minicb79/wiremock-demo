package com.minicdesign.wiremockdemo.order.adapter.out.http

import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort
import com.minicdesign.wiremockdemo.order.inventoryapi.model.InventoryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import java.net.InetSocketAddress
import java.net.ProxySelector
import java.net.URI
import java.net.http.HttpClient

@Component
class InventoryHttpAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${services.inventory.base-url}") inventoryBaseUrl: String,
    @Value("\${services.wiremock.base-url:}") wiremockBaseUrl: String,
    @Value("\${feature.wiremock-as-proxy:false}") asProxyEnabled: Boolean,
    @Value("\${feature.wiremock-via-interceptor:false}") viaInterceptorEnabled: Boolean,
    @Value("\${feature.wiremock-use-forward-proxy:false}") useForwardProxy: Boolean,
    wiremockDivertingInterceptorProvider: ObjectProvider<WiremockDivertingInterceptor>,
) : InventoryPort {
    private val logger = LoggerFactory.getLogger(InventoryHttpAdapter::class.java)

    private val restClient: RestClient =
        run {
            val finalBaseUrl =
                if (asProxyEnabled) {
                    if (useForwardProxy) {
                        val proxyUri = URI.create(wiremockBaseUrl)
                        val host = proxyUri.host ?: "localhost"
                        val port =
                            if (proxyUri.port != -1) {
                                proxyUri.port
                            } else if (proxyUri.scheme == "https") {
                                443
                            } else {
                                80
                            }
                        logger.info(
                            "[InventoryHttpAdapter] Wiremock as Forward Proxy is ENABLED. Routing traffic through HTTP Proxy at {}:{}",
                            host,
                            port,
                        )
                        val httpClient =
                            HttpClient
                                .newBuilder()
                                .version(HttpClient.Version.HTTP_1_1)
                                .proxy(ProxySelector.of(InetSocketAddress(host, port)))
                                .build()
                        restClientBuilder.requestFactory(JdkClientHttpRequestFactory(httpClient))
                        inventoryBaseUrl
                    } else {
                        logger.info(
                            "[InventoryHttpAdapter] Wiremock as Reverse Proxy is ENABLED. Routing traffic to WireMock: {}",
                            wiremockBaseUrl,
                        )
                        wiremockBaseUrl
                    }
                } else {
                    inventoryBaseUrl
                }

            if (!asProxyEnabled && viaInterceptorEnabled) {
                wiremockDivertingInterceptorProvider.ifAvailable { interceptor ->
                    logger.info("[InventoryHttpAdapter] WireMock Diverting Interceptor is ENABLED. Attaching interceptor.")
                    restClientBuilder.requestInterceptor(interceptor)
                }
            }

            restClientBuilder
                .baseUrl(finalBaseUrl)
                .build()
        }

    override fun getAvailableQuantity(productId: String): Int? {
        logger.info("Calling inventory-service for productId={}", productId)
        return try {
            val response =
                restClient
                    .get()
                    .uri("/inventory/{productId}", productId)
                    .retrieve()
                    .body(InventoryResponse::class.java)

            val quantity = response?.quantity
            logger.info("Inventory response for productId={} quantity={} available={}", productId, response?.quantity, response?.available)

            // Return null if not available (treated as out of stock / not orderable)
            if (response?.available == true) quantity else 0
        } catch (ex: HttpClientErrorException.NotFound) {
            logger.warn("Product not found in inventory-service: productId={}", productId)
            null
        } catch (ex: Exception) {
            logger.error("Error calling inventory-service for productId={}: {}", productId, ex.message)
            throw ex
        }
    }
}
