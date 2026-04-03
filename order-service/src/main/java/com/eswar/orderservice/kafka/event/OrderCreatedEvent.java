package com.eswar.orderservice.kafka.event;

import org.apache.commons.lang3.text.translate.UnicodeUnescaper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID traceId,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount,
        Set<OrderItemEvent> items
) {
    // Convenience constructor: service only passes eventId (correlationId)
    public OrderCreatedEvent(UUID eventId,
                             UUID orderId,
                             UUID customerId,
                             BigDecimal totalAmount,
                             Set<OrderItemEvent> items) {
        this(eventId, null, orderId, customerId, totalAmount, items);
        // traceId will be set later by producer
    }
}
