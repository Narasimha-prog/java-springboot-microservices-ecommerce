package com.eswar.paymentservice.kafka.producer;

import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.events.OrderStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void sendPaymentStatus(UUID orderId, EventStatus status, String message,String paymentReference) {

        OrderStatusEvent event = new OrderStatusEvent(
                UUID.randomUUID(),
                orderId,
                "PAYMENT",
                status.name(),
                message,
                paymentReference
        );

        kafkaTemplate.send("order-events", event);
    }
}
