package com.eswar.paymentservice.kafka.producer;

import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import com.eswar.paymentservice.kafka.events.OrderStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPaymentStatus(OrderStatusEvent event) {
log.info("Event is produced here..{}",event.status());
        kafkaTemplate.send("order-events", event);


    }
}
