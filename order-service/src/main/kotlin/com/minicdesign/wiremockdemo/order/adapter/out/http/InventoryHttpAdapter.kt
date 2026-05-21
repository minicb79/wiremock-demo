package com.minicdesign.wiremockdemo.order.adapter.out.http

import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort
import com.minicdesign.wiremockdemo.order.generated.inventoryapi.model.InventoryResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient

@Component
class InventoryHttpAdapter(
    restClientBuilder: RestClient.Builder,
    @Value("\${services.inventory.base-url}") inventoryBaseUrl: String,
) : InventoryPort {
    private val logger = LoggerFactory.getLogger(InventoryHttpAdapter::class.java)

    private val restClient: RestClient =
        restClientBuilder
            .baseUrl(inventoryBaseUrl)
            .build()

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
