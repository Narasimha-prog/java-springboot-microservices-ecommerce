package com.eswar.inventoryservice.service;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.entity.InventoryEntity;
import com.eswar.inventoryservice.exception.InventoryNotFoundException;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.OrderItem;
import com.eswar.inventoryservice.mapper.IInventoryMapper;
import com.eswar.inventoryservice.repository.IInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryServiceImp implements IInventoryService{

    private  final IInventoryRepository inventoryRepository;
    private final IInventoryMapper  inventoryMapper;

    public boolean reserveStock(OrderCreatedEvent event) {

        for (OrderItem item : event.items()) {

            InventoryEntity inventory =
                    inventoryRepository.findById(item.productId())
                            .orElseThrow();

            if (inventory.getAvailableQuantity() < item.quantity()) {
                return false;
            }
        }

        // reserve stock
        for (OrderItem item : event.items()) {

            InventoryEntity inventory =
                    inventoryRepository.findById(item.productId()).orElseThrow();

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - item.quantity()
            );

            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + item.quantity()
            );

            inventoryRepository.save(inventory);
        }

        return true;
    }


    public InventoryDto createInventory(InventoryDto dto) {

        InventoryEntity entity = inventoryMapper.toEntity(dto);

        InventoryEntity saved = inventoryRepository.save(entity);

        return inventoryMapper.toDto(saved);
    }

    public InventoryDto getInventory(UUID productId) {

        InventoryEntity entity = inventoryRepository
                .findById(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found"));

        return inventoryMapper.toDto(entity);
    }
}
