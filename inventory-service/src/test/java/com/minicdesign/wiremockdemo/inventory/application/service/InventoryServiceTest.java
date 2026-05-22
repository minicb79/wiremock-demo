package com.minicdesign.wiremockdemo.inventory.application.service;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import com.minicdesign.wiremockdemo.inventory.domain.port.out.InventoryRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InventoryServiceTest {

    @Test
    public void shouldReturnItemWhenExists() {
        InventoryRepository repository = mock(InventoryRepository.class);
        InventoryItem mockItem = new InventoryItem("PROD-001", 100, true);
        when(repository.findByProductId("PROD-001")).thenReturn(mockItem);

        InventoryService service = new InventoryService(repository);
        InventoryItem result = service.getInventory("PROD-001");

        assertNotNull(result);
        assertEquals(100, result.quantity());
        assertTrue(result.available());
    }

    @Test
    public void shouldReturnNullWhenNotExists() {
        InventoryRepository repository = mock(InventoryRepository.class);
        when(repository.findByProductId("UNKNOWN")).thenReturn(null);

        InventoryService service = new InventoryService(repository);
        InventoryItem result = service.getInventory("UNKNOWN");

        assertNull(result);
    }
}
