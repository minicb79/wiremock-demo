package com.minicdesign.wiremockdemo.order.domain.port.out;

public interface InventoryPort {
	/**
	 * Returns the available stock quantity for the given product, or null if the
	 * product does not exist.
	 */
	Integer getAvailableQuantity(String productId);
}
