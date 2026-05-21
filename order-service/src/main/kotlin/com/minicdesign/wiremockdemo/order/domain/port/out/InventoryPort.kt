package com.minicdesign.wiremockdemo.order.domain.port.out

interface InventoryPort {
    /**
     * Returns the available stock quantity for the given product,
     * or null if the product does not exist.
     */
    fun getAvailableQuantity(productId: String): Int?
}
