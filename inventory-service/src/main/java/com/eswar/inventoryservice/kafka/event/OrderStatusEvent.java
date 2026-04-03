package com.eswar.inventoryservice.kafka.event;

import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;

import java.util.UUID;

public record OrderStatusEvent(
        UUID eventId,
        UUID orderId,
        EventType eventType,   // INVENTORY, PAYMENT
        EventStatus status,      // SUCCESS, FAILED
        String message      // optional
) {
}
