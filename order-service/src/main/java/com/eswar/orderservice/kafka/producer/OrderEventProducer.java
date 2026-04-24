package com.eswar.orderservice.kafka.producer;

import com.eswar.orderservice.kafka.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        // Generate a new traceId for this send attempt
        UUID newTraceId = UUID.randomUUID();

        OrderCreatedEvent eventWithNewTraceId = new OrderCreatedEvent(
                event.eventId(),   // same correlationId for workflow/order
                newTraceId,       // new traceId for this attempt
                event.orderId(),
                event.customerId(),
                event.totalAmount(),
                event.items()
        );
      log.info("order-event is created and send ...");
        kafkaTemplate.send("order-created", eventWithNewTraceId);
    }
}
