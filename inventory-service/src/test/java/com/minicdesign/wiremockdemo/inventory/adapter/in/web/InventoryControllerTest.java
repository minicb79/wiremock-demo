package com.minicdesign.wiremockdemo.inventory.adapter.in.web;

import com.minicdesign.wiremockdemo.inventory.domain.model.InventoryItem;
import com.minicdesign.wiremockdemo.inventory.domain.port.in.GetInventoryUseCase;
import com.minicdesign.wiremockdemo.inventory.inventoryapi.model.InventoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryControllerTest {

    @Test
    void shouldReturnInventoryItem() {
        GetInventoryUseCase useCase = mock(GetInventoryUseCase.class);
        when(useCase.getInventory("PROD-001")).thenReturn(new InventoryItem("PROD-001", 100, true));
        InventoryController controller = new InventoryController(useCase);
        
        ResponseEntity<InventoryResponse> response = controller.getInventory("PROD-001");
        
        assertEquals(200, response.getStatusCode().value());
        assertEquals(100, response.getBody().getQuantity());
    }

    @Test
    void shouldReturnNotFound() {
        GetInventoryUseCase useCase = mock(GetInventoryUseCase.class);
        when(useCase.getInventory("UNKNOWN")).thenReturn(null);
        InventoryController controller = new InventoryController(useCase);
        
        ResponseEntity<InventoryResponse> response = controller.getInventory("UNKNOWN");
        
        assertEquals(404, response.getStatusCode().value());
    }
}
