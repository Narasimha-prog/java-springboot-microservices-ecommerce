package com.eswar.orderservice.kafka.consumer;


import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import com.eswar.orderservice.kafka.service.OrderKafkaService;
import com.eswar.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStatusEventHandler {

    private final IOrderService orderService;

    @KafkaListener(topics = "order-events", groupId = "order-group",containerFactory ="orderListenerFactory" )
    public void handleStatusEvent(OrderStatusEvent event, Acknowledgment acknowledgment) {
    try{
        orderService.handleOrderStatusEvent(event);
        acknowledgment.acknowledge();
        log.info("Successfully processed and acknowledged: {}", event.eventId());
      } catch (Exception ex) {
        acknowledgment.acknowledge();
        log.error("failed processed and acknowledged: {}", event.eventId());
       }
 }




}