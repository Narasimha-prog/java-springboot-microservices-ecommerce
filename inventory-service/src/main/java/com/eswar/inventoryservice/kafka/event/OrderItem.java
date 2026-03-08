package com.eswar.inventoryservice.kafka.event;

import java.util.UUID;

public record OrderItem(

        UUID productId,
        Integer quantity
) {
}
