package com.eswar.inventoryservice.kafka.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        UUID orderId,
        List<OrderItem> items
) {}

