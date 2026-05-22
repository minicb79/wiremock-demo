package com.minicdesign.wiremockdemo.inventory.adapter.out.persistence;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import com.minicdesign.wiremockdemo.inventory.domain.port.out.InventoryRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class InMemoryInventoryRepository implements InventoryRepository {
    private final Map<String, InventoryItem> stock = Map.of(
            "PROD-001", new InventoryItem("PROD-001", 100, true),
            "PROD-002", new InventoryItem("PROD-002", 5, true),
            "PROD-003", new InventoryItem("PROD-003", 0, false)
    );

    @Override
    public InventoryItem findByProductId(String productId) {
        return stock.get(productId);
    }
}
