package com.minicdesign.wiremockdemo.inventory.domain.port.out

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem

interface InventoryRepository {
    fun findByProductId(productId: String): InventoryItem?
}
