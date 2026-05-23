package com.minicdesign.wiremockdemo.inventory.application.service;

import java.io.Serial;

public class InventoryItemNotFoundException extends RuntimeException {
	@Serial
    private static final long serialVersionUID = 1L;

	public InventoryItemNotFoundException(String productId) {
		super("No inventory found for productId: " + productId);
	}
}
