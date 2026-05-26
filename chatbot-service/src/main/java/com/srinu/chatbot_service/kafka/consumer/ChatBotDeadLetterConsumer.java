package com.srinu.chatbot_service.kafka.consumer;


import com.srinu.chatbot_service.kafka.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ChatBotDeadLetterConsumer {

    @KafkaListener(topics = "product-events-dlt", groupId = "inventory-dlq-group")
    public void handleDeadLetters(
            ProductCreatedEvent event,
            // Use the EXACT strings from your terminal output
            @Header(name = "kafka_dlt-exception-message", required = false) String error,
            @Header(name = "kafka_dlt-original-offset", required = false) byte[] offset,
            Acknowledgment ack
    ) {
        log.error("🚨 DLQ RECEIVED");
        log.error("Original Event ID: {}", event.eventId());
        log.error("Failure Reason: {}", error);

        // Acknowledge to clear the 2 messages we saw in the CLI
        ack.acknowledge();
    }
}
