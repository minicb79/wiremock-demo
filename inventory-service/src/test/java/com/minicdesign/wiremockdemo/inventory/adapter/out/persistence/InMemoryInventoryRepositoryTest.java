package com.minicdesign.wiremockdemo.inventory.adapter.out.persistence;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryInventoryRepositoryTest {

	@Test
	void shouldFindItem() {
		InMemoryInventoryRepository repo = new InMemoryInventoryRepository();
		InventoryItem item = repo.findByProductId("PROD-001");
		assertNotNull(item);
		assertEquals(100, item.quantity());
	}

	@Test
	void shouldNotFindItem() {
		InMemoryInventoryRepository repo = new InMemoryInventoryRepository();
		InventoryItem item = repo.findByProductId("UNKNOWN");
		assertNull(item);
	}
}
