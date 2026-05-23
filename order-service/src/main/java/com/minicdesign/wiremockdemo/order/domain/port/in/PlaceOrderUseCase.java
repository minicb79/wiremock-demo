package com.minicdesign.wiremockdemo.order.domain.port.in;

import com.minicdesign.wiremockdemo.order.domain.model.Order;

public interface PlaceOrderUseCase {
	Order placeOrder(String productId, int quantity);
}
