package com.minicdesign.wiremockdemo.order.application.service;

import com.minicdesign.wiremockdemo.order.domain.model.Order;
import com.minicdesign.wiremockdemo.order.domain.model.OrderStatus;
import com.minicdesign.wiremockdemo.order.domain.port.in.PlaceOrderUseCase;
import com.minicdesign.wiremockdemo.order.domain.port.out.InventoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService implements PlaceOrderUseCase {
    private final InventoryPort inventoryPort;

    @Override
    public Order placeOrder(String productId, int quantity) {
        log.info("Placing order for productId={} quantity={}", productId, quantity);

        Integer availableQuantity = inventoryPort.getAvailableQuantity(productId);

        if (availableQuantity == null) {
            log.warn("Product not found in inventory: productId={}", productId);
            throw new ProductNotFoundException(productId);
        }

        String rawUuid = UUID.randomUUID().toString();
        String orderId = "ORD-" + rawUuid.substring(0, Math.min(rawUuid.length(), 8));

        if (availableQuantity >= quantity) {
            log.info("Order confirmed orderId={} productId={} quantity={}", orderId, productId, quantity);
            return new Order(orderId, productId, quantity, OrderStatus.CONFIRMED);
        } else {
            log.warn(
                    "Insufficient stock for orderId={} productId={} requested={} available={}",
                    orderId,
                    productId,
                    quantity,
                    availableQuantity
            );
            return new Order(orderId, productId, quantity, OrderStatus.REJECTED);
        }
    }
}
