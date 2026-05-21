package com.minicdesign.wiremockdemo.order.domain.port.`in`

import com.minicdesign.wiremockdemo.order.domain.model.Order

interface PlaceOrderUseCase {
    fun placeOrder(
        productId: String,
        quantity: Int,
    ): Order
}
