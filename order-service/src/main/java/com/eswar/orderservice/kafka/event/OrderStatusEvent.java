package com.eswar.orderservice.kafka.event;

import com.eswar.orderservice.kafka.constatnts.EventStatus;
import com.eswar.orderservice.kafka.constatnts.EventType;

import java.awt.*;
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
