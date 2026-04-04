package com.eswar.paymentservice.kafka.producer;

import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import com.eswar.paymentservice.kafka.events.OrderStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentStatus(OrderStatusEvent event) {

        kafkaTemplate.send("order-events", event);


    }
}
