package com.eswar.paymentservice.kafka.events;

import java.util.UUID;

public record OrderStatusEvent(
        UUID eventId,
        UUID orderId,
        String eventType,   // INVENTORY, PAYMENT
        String status,      // SUCCESS, FAILED
        String message  ,    // optional
        String paymentReference
) {
}
