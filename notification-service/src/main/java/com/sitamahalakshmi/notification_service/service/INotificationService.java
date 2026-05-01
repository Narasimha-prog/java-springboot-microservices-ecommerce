package com.sitamahalakshmi.notification_service.service;

import com.sitamahalakshmi.notification_service.kafka.events.OrderCreatedEvent;
import com.sitamahalakshmi.notification_service.kafka.events.OrderStatusEvent;

public interface INotificationService {
    /**
     * Handles the initial order placement notification.
     * Triggered immediately after the Order Service saves the order.
     */
    void handleOrderCreatedEvent(OrderCreatedEvent event);

    /**
     * Handles status updates from the Saga flow (Payment, Inventory).
     * This will decide whether to send a "Success", "Failed", or "Refund" email.
     */
    void handleOrderStatusEvent(OrderStatusEvent event);
}
