package com.eswar.orderservice.kafka.consumer;


import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import com.eswar.orderservice.kafka.service.OrderKafkaService;
import com.eswar.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusEventHandler {

    private final IOrderService orderService;

    @KafkaListener(topics = "order-events", groupId = "order-group",containerFactory ="orderListenerFactory" )
    public void handleStatusEvent(OrderStatusEvent event) {

        orderService.handleOrderStatusEvent(event);
    }




}