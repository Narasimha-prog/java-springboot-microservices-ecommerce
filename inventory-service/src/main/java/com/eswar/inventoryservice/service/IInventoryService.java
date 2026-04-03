package com.eswar.inventoryservice.service;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.dto.PageResponse;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface IInventoryService {
    void handleOrderCreatedEvent(@NonNull OrderCreatedEvent event);
    InventoryDto createInventory(InventoryDto dto);
    InventoryDto getInventory(UUID productId);
    PageResponse<InventoryDto> getAllInventories(Pageable pageable);

}
