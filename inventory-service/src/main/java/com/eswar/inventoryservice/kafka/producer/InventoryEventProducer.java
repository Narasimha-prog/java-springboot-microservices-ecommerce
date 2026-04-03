package com.eswar.inventoryservice.kafka.producer;

import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.OrderStatusEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderStatusEvent(OrderStatusEvent event) {
        kafkaTemplate.send("order-events", event);
        log.info("Produced OrderStatusEvent to topic {}: {}", "order-events", event);
    }

}
