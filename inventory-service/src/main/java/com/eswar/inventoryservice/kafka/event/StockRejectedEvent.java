package com.eswar.inventoryservice.kafka.event;

import java.util.UUID;

public record StockRejectedEvent(
        UUID orderId,
        String reason
) {
}
