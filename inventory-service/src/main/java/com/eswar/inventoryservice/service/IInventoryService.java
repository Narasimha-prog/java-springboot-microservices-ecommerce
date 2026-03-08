package com.eswar.inventoryservice.service;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;

import java.util.UUID;

public interface IInventoryService {
    boolean reserveStock(OrderCreatedEvent event);
    InventoryDto createInventory(InventoryDto dto);
    InventoryDto getInventory(UUID productId);
}
