package com.eswar.inventoryservice.kafka.consumer;

import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import com.eswar.inventoryservice.kafka.event.OrderStatusEvent;
import com.eswar.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {
    private final IInventoryService inventoryService;
    @KafkaListener(topics = "order-events", groupId = "inventory-service-group",containerFactory ="orderListenerFactory" )
    public void handleOrderStatus(OrderStatusEvent event, Acknowledgment ack) {
        try {
            log.info("Received OrderStatusEvent: {} for order {}", event.eventType(), event.orderId());

            // 1. Filter out its own messages (Idempotency check)
            if (event.eventType() == EventType.INVENTORY) {
                log.debug("Skipping self-produced inventory event for order {}", event.orderId());
                ack.acknowledge();
                return;
            }

            // 2. Only act on PAYMENT events
            if (event.eventType() == EventType.PAYMENT) {
                if (event.status() == EventStatus.FAILED) {
                    log.info("Payment failed for order {}. Releasing reserved stock.", event.orderId());
                    inventoryService.releaseReservedStock(event.orderId());
                } else if (event.status() == EventStatus.PROCESSED) {
                    log.info("Payment success for order {}. Committing stock.", event.orderId());
                    inventoryService.commitStock(event.orderId());
                }
            }

            // 3. SUCCESS ACK: Move offset forward
            ack.acknowledge();
            log.info("Successfully processed and acknowledged status event for order: {}", event.orderId());

        } catch (Exception e) {
            log.error("Error processing OrderStatusEvent for order {}: {}", event.orderId(), e.getMessage());

        /* 🔹 FAILURE ACK:
           Just like your OrderCreated listener, we acknowledge here
           to avoid infinite loops, assuming your service records
           the failure in an internal ledger or DLT.
        */
            ack.acknowledge();
        }
    }
}
