package com.eswar.inventoryservice.kafka.service;

import com.eswar.inventoryservice.entity.EventEntity;
import com.eswar.inventoryservice.entity.InventoryEntity;
import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.OrderStatusEvent;
import com.eswar.inventoryservice.kafka.event.StockRejectedEvent;
import com.eswar.inventoryservice.kafka.event.StockReservedEvent;
import com.eswar.inventoryservice.kafka.producer.InventoryEventProducer;
import com.eswar.inventoryservice.repository.IEventRepository;
import com.eswar.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryKafkaService {

  private final InventoryEventProducer inventoryEventProducer;

    public void sendOrderStatusEvent(EventEntity entity, boolean reserved, String message) {

        EventStatus status = reserved ? EventStatus.PROCESSED : EventStatus.FAILED;

        OrderStatusEvent statusEvent = new OrderStatusEvent(
                entity.getEventId(),
                entity.getOrderId(),      // orderId
                EventType.INVENTORY,       // type INVENTORY
                status,                    // SUCCESS or FAILED
                message                    // optional message
        );

        inventoryEventProducer.sendOrderStatusEvent(statusEvent);

        log.info("Sent OrderStatusEvent for order {} with status {}", entity.getOrderId(), status);
    }
}
