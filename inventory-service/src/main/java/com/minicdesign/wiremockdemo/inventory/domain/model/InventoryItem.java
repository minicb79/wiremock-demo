package com.minicdesign.wiremockdemo.inventory.domain.model;

public record InventoryItem(String productId, int quantity, boolean available) {
}
