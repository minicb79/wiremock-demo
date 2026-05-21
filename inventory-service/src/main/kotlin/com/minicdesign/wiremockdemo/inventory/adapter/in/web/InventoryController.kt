package com.minicdesign.wiremockdemo.inventory.adapter.`in`.web

import com.minicdesign.wiremockdemo.inventory.domain.port.`in`.GetInventoryUseCase
import com.minicdesign.wiremockdemo.inventory.generated.inventoryapi.api.InventoryApi
import com.minicdesign.wiremockdemo.inventory.generated.inventoryapi.model.InventoryResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class InventoryController(
    private val getInventoryUseCase: GetInventoryUseCase,
) : InventoryApi {
    private val logger = LoggerFactory.getLogger(InventoryController::class.java)

    override fun getInventory(productId: String): ResponseEntity<InventoryResponse> {
        logger.info("GET /inventory/{} received", productId)
        val item =
            getInventoryUseCase.getInventory(productId)
                ?: return ResponseEntity.notFound().build()

        val response =
            InventoryResponse(
                item.productId,
                item.quantity,
                item.available,
            )
        return ResponseEntity.ok(response)
    }
}
