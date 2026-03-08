package com.eswar.orderservice.kafka.event;

import java.util.UUID;

public record StockReservedEvent(
        UUID orderId
) {
}
