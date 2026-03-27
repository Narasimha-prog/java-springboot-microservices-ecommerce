package com.eswar.paymentservice.kafka.consumer;

import com.eswar.paymentservice.kafka.events.OrderCreatedEvent;
import com.eswar.paymentservice.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final IPaymentService paymentService;

    @KafkaListener(topics = "order-created", groupId = "payment-group-v1")
    public void consume(OrderCreatedEvent event) {
        log.info("Received order event: {}", event);

        paymentService.processPayment(event);
    }
}