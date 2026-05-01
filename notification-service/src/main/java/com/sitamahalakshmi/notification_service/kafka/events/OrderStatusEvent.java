package com.sitamahalakshmi.notification_service.kafka.events;

import    com.sitamahalakshmi.notification_service.kafka.constatnts.EventStatus;
import  com.sitamahalakshmi.notification_service.kafka.constatnts.EventType;


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
