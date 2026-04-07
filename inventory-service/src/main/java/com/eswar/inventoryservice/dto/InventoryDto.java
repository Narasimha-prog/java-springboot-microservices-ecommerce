package com.eswar.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;
@Builder
public record InventoryDto (
        @NotNull(message = "Product ID is mandatory")
        UUID productId,

        @NotNull(message = "Available quantity cannot be null")
        @Min(value = 0, message = "Available quantity must be 0 or greater")
        Integer availableQuantity,

        @NotNull(message = "Reserved quantity cannot be null")
        @Min(value = 0, message = "Reserved quantity must be 0 or greater")
        Integer reservedQuantity
){
}
