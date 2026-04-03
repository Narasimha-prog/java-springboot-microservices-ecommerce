package com.eswar.paymentservice.kafka.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        UUID eventId,
        UUID traceId,
        UUID orderId,
        UUID customerId,
        BigDecimal totalAmount
) {
}
