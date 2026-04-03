package com.eswar.paymentservice.kafka.events;

import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;

import java.util.UUID;

public record OrderStatusEvent(
        UUID eventId,
        UUID orderId,
        EventType eventType,   // INVENTORY, PAYMENT
        EventStatus status,      // SUCCESS, FAILED
        String message  ,    // optional
        String paymentReference
) {
}
