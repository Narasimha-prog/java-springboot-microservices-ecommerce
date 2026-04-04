package com.eswar.paymentservice.kafka.consumer;

import com.eswar.paymentservice.kafka.events.OrderCreatedEvent;
import com.eswar.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final IPaymentService paymentService;

    @KafkaListener(topics = "order-created", groupId = "payment-group-v1")
    public void consume(OrderCreatedEvent event, Acknowledgment acknowledgment) {
        log.info("Received order event: {}", event);

        paymentService.handleOrderCreatedEvent(event);

        try {
            // Call the Orchestrator
            paymentService.handleOrderCreatedEvent(event);

            // 🔹 SUCCESS ACK: Business logic worked, offset moves forward.
            acknowledgment.acknowledge();
            log.info("Successfully processed and acknowledged: {}", event.eventId());

        } catch (Exception e) {
            log.error("Error in payment processing for {}: {}", event.eventId(), e.getMessage());

        /* 🔹 FAILURE ACK:
           Because 'recordFailure' already saved the error to our DB,
           and the 'ErrorHandler' moved the message to the DLQ (after 0,1,2 retries),
           we MUST acknowledge here to prevent an infinite loop.
        */
            acknowledgment.acknowledge();
        }
    }
}