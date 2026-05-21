package com.minicdesign.wiremockdemo.inventory.application.service

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem
import com.minicdesign.wiremockdemo.inventory.domain.port.`in`.GetInventoryUseCase
import com.minicdesign.wiremockdemo.inventory.domain.port.out.InventoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class InventoryService(
    private val inventoryRepository: InventoryRepository,
) : GetInventoryUseCase {
    private val logger = LoggerFactory.getLogger(InventoryService::class.java)

    override fun getInventory(productId: String): InventoryItem? {
        logger.info("Fetching inventory for productId={}", productId)
        val item = inventoryRepository.findByProductId(productId)
        if (item == null) {
            logger.warn("No inventory found for productId={}", productId)
        } else {
            logger.info("Inventory found for productId={} quantity={} available={}", productId, item.quantity, item.available)
        }
        return item
    }
}
