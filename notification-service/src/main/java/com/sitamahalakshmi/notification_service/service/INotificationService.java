package com.sitamahalakshmi.notification_service.service;

import com.sitamahalakshmi.notification_service.kafka.events.OrderCreatedEvent;

public interface INotificationService {
    void handleOrderCreatedEvent(OrderCreatedEvent event);
}
