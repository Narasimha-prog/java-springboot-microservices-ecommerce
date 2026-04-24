package com.eswar.inventoryservice.kafka.consumer;

import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.StockRejectedEvent;
import com.eswar.inventoryservice.kafka.event.StockReservedEvent;
import com.eswar.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final IInventoryService inventoryService;

    @KafkaListener(topics = "order-created",groupId = "inventory-group")
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {

        try {
            // Call the Orchestrator
            inventoryService.handleOrderCreatedEvent(event);

            // 🔹 SUCCESS ACK: Business logic worked, offset moves forward.
            ack.acknowledge();
            log.info("Successfully processed and acknowledged: {}", event.eventId());

        } catch (Exception e) {
            log.error("Error in inventory processing for {}: {}", event.eventId(), e.getMessage());

        /* 🔹 FAILURE ACK:
           Because 'recordFailure' already saved the error to our DB,
           and the 'ErrorHandler' moved the message to the DLQ (after 0,1,2 retries),
           we MUST acknowledge here to prevent an infinite loop.
        */
            ack.acknowledge();
        }

    }
}
