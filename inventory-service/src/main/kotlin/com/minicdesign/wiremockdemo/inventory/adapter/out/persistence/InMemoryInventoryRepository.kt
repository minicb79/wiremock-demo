package com.minicdesign.wiremockdemo.inventory.adapter.out.persistence

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem
import com.minicdesign.wiremockdemo.inventory.domain.port.out.InventoryRepository
import org.springframework.stereotype.Repository

@Repository
class InMemoryInventoryRepository : InventoryRepository {
    private val stock: Map<String, InventoryItem> =
        mapOf(
            "PROD-001" to InventoryItem(productId = "PROD-001", quantity = 100, available = true),
            "PROD-002" to InventoryItem(productId = "PROD-002", quantity = 5, available = true),
            "PROD-003" to InventoryItem(productId = "PROD-003", quantity = 0, available = false),
        )

    override fun findByProductId(productId: String): InventoryItem? = stock[productId]
}
