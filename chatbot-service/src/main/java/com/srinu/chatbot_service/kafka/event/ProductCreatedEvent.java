package com.srinu.chatbot_service.kafka.event;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductCreatedEvent(
        UUID eventId,        // Unique ID for the event itself
        UUID traceId,        // For logging and tracking attempts
        UUID productId,      // Target entity tracking
        String sku,
        String name,
        String description,
        BigDecimal price,
        String categoryName
) {}
