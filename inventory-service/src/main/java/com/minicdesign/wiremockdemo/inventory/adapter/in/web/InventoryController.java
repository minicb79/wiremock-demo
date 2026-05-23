package com.minicdesign.wiremockdemo.inventory.adapter.in.web;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import com.minicdesign.wiremockdemo.inventory.domain.port.in.GetInventoryUseCase;
import com.minicdesign.wiremockdemo.inventory.inventoryapi.api.InventoryApi;
import com.minicdesign.wiremockdemo.inventory.inventoryapi.model.InventoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class InventoryController implements InventoryApi {
	private final GetInventoryUseCase getInventoryUseCase;

	@Override
	public ResponseEntity<InventoryResponse> getInventory(String productId) {
		log.info("GET /v1/inventory/{} received", productId);
		InventoryItem item = getInventoryUseCase.getInventory(productId);
		InventoryResponse response = new InventoryResponse(item.productId(), item.quantity(), item.available());
		return ResponseEntity.ok(response);
	}
}
