package com.minicdesign.wiremockdemo.inventory.application.service;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import com.minicdesign.wiremockdemo.inventory.domain.port.in.GetInventoryUseCase;
import com.minicdesign.wiremockdemo.inventory.domain.port.out.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements GetInventoryUseCase {
    private final InventoryRepository inventoryRepository;

    @Override
    public InventoryItem getInventory(String productId) {
        log.info("Fetching inventory for productId={}", productId);
        InventoryItem item = inventoryRepository.findByProductId(productId);
        if (item == null) {
            log.warn("No inventory found for productId={}", productId);
        } else {
            log.info("Inventory found for productId={} quantity={} available={}", productId, item.quantity(), item.available());
        }
        return item;
    }
}
