package com.minicdesign.wiremockdemo.inventory.domain.port.`in`

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem

fun interface GetInventoryUseCase {
    fun getInventory(productId: String): InventoryItem?
}
