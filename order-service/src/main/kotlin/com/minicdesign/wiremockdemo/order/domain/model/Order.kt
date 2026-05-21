package com.minicdesign.wiremockdemo.order.domain.model

data class Order(
    val orderId: String,
    val productId: String,
    val quantity: Int,
    val status: OrderStatus,
)

enum class OrderStatus {
    CONFIRMED,
    REJECTED,
}
