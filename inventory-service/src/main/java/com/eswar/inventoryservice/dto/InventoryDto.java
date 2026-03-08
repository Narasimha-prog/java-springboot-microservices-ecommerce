package com.eswar.inventoryservice.dto;

import lombok.Builder;

import java.util.UUID;
@Builder
public record InventoryDto (
         UUID productId,
         Integer availableQuantity,
         Integer reservedQuantity
){
}
