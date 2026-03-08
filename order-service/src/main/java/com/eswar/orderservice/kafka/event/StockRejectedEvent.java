package com.eswar.orderservice.kafka.event;

import java.util.UUID;

public record StockRejectedEvent(
        UUID orderId,
        String reason
) {
}
