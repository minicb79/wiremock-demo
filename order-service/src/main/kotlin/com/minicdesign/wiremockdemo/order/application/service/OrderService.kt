package com.minicdesign.wiremockdemo.order.application.service

import com.minicdesign.wiremockdemo.order.domain.model.Order
import com.minicdesign.wiremockdemo.order.domain.model.OrderStatus
import com.minicdesign.wiremockdemo.order.domain.port.`in`.PlaceOrderUseCase
import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class OrderService(
    private val inventoryPort: InventoryPort,
) : PlaceOrderUseCase {
    private val logger = LoggerFactory.getLogger(OrderService::class.java)

    override fun placeOrder(
        productId: String,
        quantity: Int,
    ): Order {
        logger.info("Placing order for productId={} quantity={}", productId, quantity)

        val availableQuantity = inventoryPort.getAvailableQuantity(productId)

        if (availableQuantity == null) {
            logger.warn("Product not found in inventory: productId={}", productId)
            throw ProductNotFoundException(productId)
        }

        val orderId = "ORD-${UUID.randomUUID().toString().take(8)}"

        return if (availableQuantity >= quantity) {
            logger.info("Order confirmed orderId={} productId={} quantity={}", orderId, productId, quantity)
            Order(orderId = orderId, productId = productId, quantity = quantity, status = OrderStatus.CONFIRMED)
        } else {
            logger.warn(
                "Insufficient stock for orderId={} productId={} requested={} available={}",
                orderId,
                productId,
                quantity,
                availableQuantity,
            )
            Order(orderId = orderId, productId = productId, quantity = quantity, status = OrderStatus.REJECTED)
        }
    }
}

class ProductNotFoundException(
    productId: String,
) : RuntimeException("Product not found: $productId")
