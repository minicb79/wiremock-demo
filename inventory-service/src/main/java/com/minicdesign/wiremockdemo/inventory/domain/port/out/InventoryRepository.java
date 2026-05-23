package com.minicdesign.wiremockdemo.inventory.domain.port.out;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;

public interface InventoryRepository {
	InventoryItem findByProductId(String productId);
}
