package com.minicdesign.wiremockdemo.order.domain.model;

public record Order(
    String orderId,
    String productId,
    int quantity,
    OrderStatus status
) {}
