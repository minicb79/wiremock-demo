package com.minicdesign.wiremockdemo.order.adapter.`in`.web

import com.minicdesign.wiremockdemo.order.application.service.ProductNotFoundException
import com.minicdesign.wiremockdemo.order.domain.model.OrderStatus
import com.minicdesign.wiremockdemo.order.domain.port.`in`.PlaceOrderUseCase
import com.minicdesign.wiremockdemo.order.orderapi.api.OrdersApi
import com.minicdesign.wiremockdemo.order.orderapi.model.ErrorResponse
import com.minicdesign.wiremockdemo.order.orderapi.model.OrderResponse
import com.minicdesign.wiremockdemo.order.orderapi.model.OrderResponse.StatusEnum
import com.minicdesign.wiremockdemo.order.orderapi.model.PlaceOrderRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestController
class OrderController(
    private val placeOrderUseCase: PlaceOrderUseCase,
) : OrdersApi {
    private val logger = LoggerFactory.getLogger(OrderController::class.java)

    override fun placeOrder(placeOrderRequest: PlaceOrderRequest): ResponseEntity<OrderResponse> {
        logger.info(
            "POST /orders received productId={} quantity={}",
            placeOrderRequest.productId,
            placeOrderRequest.quantity,
        )

        val order =
            placeOrderUseCase.placeOrder(
                productId = placeOrderRequest.productId,
                quantity = placeOrderRequest.quantity,
            )

        val statusEnum =
            when (order.status) {
                OrderStatus.CONFIRMED -> StatusEnum.CONFIRMED
                OrderStatus.REJECTED -> StatusEnum.REJECTED
            }

        val response = OrderResponse(order.orderId, order.productId, order.quantity, statusEnum)

        val httpStatus = if (order.status == OrderStatus.CONFIRMED) HttpStatus.CREATED else HttpStatus.CONFLICT
        return ResponseEntity.status(httpStatus).body(response)
    }
}

@RestControllerAdvice
class OrderExceptionHandler {
    private val logger = LoggerFactory.getLogger(OrderExceptionHandler::class.java)

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFound(ex: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Product not found: {}", ex.message)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse("NOT_FOUND", ex.message ?: "Product not found"),
        )
    }
}
