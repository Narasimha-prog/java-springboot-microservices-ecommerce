package com.srinu.chatbot_service.kafka.consumer;

import com.srinu.chatbot_service.kafka.event.ProductCreatedEvent;
import com.srinu.chatbot_service.service.IChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    private final IChatBotService chatBotService;

    @KafkaListener(topics = "product-created",groupId = "chatbot-group",containerFactory = "orderListenerFactory")
    public void handleOrderCreated(ProductCreatedEvent event, Acknowledgment ack) {

        try {
            // Call the Orchestrator
            chatBotService.handleProductCreatedEvent(event);

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
