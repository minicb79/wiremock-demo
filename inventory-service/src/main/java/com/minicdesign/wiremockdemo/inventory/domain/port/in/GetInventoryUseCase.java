package com.minicdesign.wiremockdemo.inventory.domain.port.in;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;

@FunctionalInterface
public interface GetInventoryUseCase {
	InventoryItem getInventory(String productId);
}
