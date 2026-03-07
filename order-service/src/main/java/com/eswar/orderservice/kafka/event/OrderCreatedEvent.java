package com.eswar.orderservice.kafka.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        Long orderId,
        UUID customerId,
        BigDecimal totalAmount,
        List<OrderItemEvent> items
) {}
