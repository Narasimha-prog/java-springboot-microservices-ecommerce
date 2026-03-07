package com.eswar.orderservice.kafka.event;

import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        Integer quantity
) {}
