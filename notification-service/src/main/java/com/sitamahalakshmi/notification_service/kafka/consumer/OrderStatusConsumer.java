package com.sitamahalakshmi.notification_service.kafka.consumer;

import com.sitamahalakshmi.notification_service.kafka.events.OrderStatusEvent;
import com.sitamahalakshmi.notification_service.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusConsumer {

    private final INotificationService notificationService;

    @KafkaListener(topics = "order-events", groupId = "notification-group-v1",containerFactory ="orderStatusListenerFactory" )
    public void handleStatusEvent(OrderStatusEvent event, Acknowledgment acknowledgment) {
        try{
            notificationService.handleOrderStatusEvent(event);
            acknowledgment.acknowledge();
            log.info("Successfully processed and acknowledged: {}", event.eventId());
        } catch (Exception ex) {
            acknowledgment.acknowledge();
            log.error("failed processed and acknowledged: {}", event.eventId());
        }
    }
}
