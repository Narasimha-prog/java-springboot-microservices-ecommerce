package com.eswar.inventoryservice.rest;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
@CrossOrigin("*")
public class InventoryRestController {

    private final IInventoryService inventoryService;

    @PostMapping
    public InventoryDto addInventory(@RequestBody InventoryDto dto) {
        return inventoryService.createInventory(dto);
    }

    @GetMapping("/{productId}")
    public InventoryDto getInventory(@PathVariable UUID productId) {
        return inventoryService.getInventory(productId);
    }
}
