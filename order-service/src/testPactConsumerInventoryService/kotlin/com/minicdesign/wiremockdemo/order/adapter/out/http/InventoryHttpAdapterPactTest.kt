package com.minicdesign.wiremockdemo.order.adapter.out.http

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.PactSpecVersion
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.ObjectProvider
import org.springframework.web.client.RestClient
import java.util.function.Supplier

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "inventory-service", pactMethod = "createPact", pactVersion = PactSpecVersion.V3)
class InventoryHttpAdapterPactTest {
    @Pact(consumer = "order-service")
    fun createPact(builder: PactDslWithProvider): RequestResponsePact =
        builder
            .given("product PROD-001 exists and has stock")
            .uponReceiving("a request for product inventory details")
            .path("/inventory/PROD-001")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(
                """
                {
                    "productId": "PROD-001",
                    "quantity": 100,
                    "available": true
                }
                """.trimIndent(),
            ).toPact()

    @Test
    fun testGetAvailableQuantity(mockServer: MockServer) {
        val restClientBuilder = RestClient.builder()
        val emptyProvider =
            object : ObjectProvider<WiremockDivertingInterceptor> {
                override fun getObject(vararg args: Any?) = throw UnsupportedOperationException()

                override fun getObject() = throw UnsupportedOperationException()

                override fun getIfAvailable() = null

                override fun getIfUnique() = null

                override fun getIfAvailable(defaultSupplier: Supplier<WiremockDivertingInterceptor>) = defaultSupplier.get()

                override fun getIfUnique(defaultSupplier: Supplier<WiremockDivertingInterceptor>) = defaultSupplier.get()

                override fun iterator(): MutableIterator<WiremockDivertingInterceptor> =
                    mutableListOf<WiremockDivertingInterceptor>().iterator()
            }

        val adapter =
            InventoryHttpAdapter(
                restClientBuilder = restClientBuilder,
                inventoryBaseUrl = mockServer.getUrl(),
                wiremockBaseUrl = "",
                asProxyEnabled = false,
                viaInterceptorEnabled = false,
                useForwardProxy = false,
                wiremockDivertingInterceptorProvider = emptyProvider,
            )

        val quantity = adapter.getAvailableQuantity("PROD-001")
        assertEquals(100, quantity)
    }
}
