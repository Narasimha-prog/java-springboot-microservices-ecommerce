package com.eswar.inventoryservice.kafka.consumer;

import com.eswar.inventoryservice.kafka.event.OrderCreatedEvent;
import com.eswar.inventoryservice.kafka.event.StockRejectedEvent;
import com.eswar.inventoryservice.kafka.event.StockReservedEvent;
import com.eswar.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final IInventoryService inventoryService;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    @KafkaListener(topics = "order-created")
    public void handleOrderCreated(OrderCreatedEvent event) {

        boolean reserved = inventoryService.reserveStock(event);

        if(reserved){
            kafkaTemplate.send(
                    "stock-reserved",
                    new StockReservedEvent(event.orderId())
            );
        }else{
            kafkaTemplate.send(
                    "stock-rejected",
                    new StockRejectedEvent(event.orderId(),"Stock not available")
            );
        }
    }
}
