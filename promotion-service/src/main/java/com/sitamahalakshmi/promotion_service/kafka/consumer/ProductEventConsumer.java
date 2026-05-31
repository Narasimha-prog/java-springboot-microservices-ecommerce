package com.sitamahalakshmi.promotion_service.kafka.consumer;

import com.sitamahalakshmi.promotion_service.kafka.event.ProductCreatedEvent;
import com.sitamahalakshmi.promotion_service.service.IPromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final IPromotionService promotionService;

    @KafkaListener(topics = "product-created",containerFactory = "productListenerFactory")
    public void handleOrderCreated(ProductCreatedEvent event, Acknowledgment ack) {

        try {
            // Call the Orchestrator
            promotionService.handleProductCreatedEvent(event);

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
