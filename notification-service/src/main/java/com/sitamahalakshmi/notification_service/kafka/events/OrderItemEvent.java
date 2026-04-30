package com.sitamahalakshmi.notification_service.kafka.events;

import java.util.UUID;

public record OrderItemEvent(
        UUID productId,
        Integer quantity
) {}
