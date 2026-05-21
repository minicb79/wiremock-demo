package com.minicdesign.wiremockdemo.inventory.domain.model

data class InventoryItem(
    val productId: String,
    val quantity: Int,
    val available: Boolean,
)
