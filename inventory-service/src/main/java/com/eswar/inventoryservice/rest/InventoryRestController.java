package com.eswar.inventoryservice.rest;

import com.eswar.inventoryservice.dto.InventoryDto;
import com.eswar.inventoryservice.dto.PageResponse;
import com.eswar.inventoryservice.service.IInventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "API for managing inventoryService in the e-commerce platform")
public class InventoryRestController {

    private final IInventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "To add Stock details",  description = "login entry for Admin")

    public ResponseEntity<InventoryDto> addInventory(@RequestBody InventoryDto dto) {
        return
                ResponseEntity.created(URI.create("/api/v1/inventory")).body(inventoryService.createInventory(dto));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "To get Stock details",  description = "login entry for user/admin")
    public ResponseEntity<InventoryDto> getInventory(@PathVariable UUID productId) {
        return ResponseEntity.ok(inventoryService.getInventory(productId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "To get all Stock details",  description = "login entry for Admin")
    public ResponseEntity<PageResponse<InventoryDto>> getAllInventory( @ParameterObject Pageable pageable){
        return ResponseEntity.ok(inventoryService.getAllInventories(pageable));
    }
    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "To update Stock details",  description = "login entry for Admin")
    public ResponseEntity<InventoryDto> updateInventory(
            @PathVariable UUID productId,
            @RequestBody InventoryDto dto) {

        return ResponseEntity.ok(inventoryService.updateInventory(productId, dto));
    }


}
