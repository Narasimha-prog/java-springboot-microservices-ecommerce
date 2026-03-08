package com.eswar.orderservice.kafka.consumer;

import com.eswar.orderservice.entity.OrderEntity;
import com.eswar.orderservice.constants.OrderStatus;
import com.eswar.orderservice.kafka.event.StockRejectedEvent;
import com.eswar.orderservice.kafka.event.StockReservedEvent;
import com.eswar.orderservice.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final IOrderRepository orderRepository;

    @KafkaListener(topics = "stock-reserved", groupId = "order-group")
    public void handleStockReserved(StockReservedEvent event){

        OrderEntity order = orderRepository.findById(event.orderId()).orElseThrow();

        order.setStatus(OrderStatus.STOCK_RESERVED);

        orderRepository.save(order);
    }

    @KafkaListener(topics = "stock-rejected", groupId = "order-group")
    public void handleStockRejected(StockRejectedEvent event){

        OrderEntity order = orderRepository.findById(event.orderId()).orElseThrow();

        order.setStatus(OrderStatus.STOCK_FAILED);

        orderRepository.save(order);
    }
}