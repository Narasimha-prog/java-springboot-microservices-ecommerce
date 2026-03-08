package com.eswar.inventoryservice.kafka.event;

import java.util.UUID;

public record StockReservedEvent(
        UUID orderId
) {
}
