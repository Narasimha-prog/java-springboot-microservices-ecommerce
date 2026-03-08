package com.eswar.orderservice.dto;

import java.util.UUID;

public record OrderItemDto(
        UUID productId,
        Integer quantity

) {
}
