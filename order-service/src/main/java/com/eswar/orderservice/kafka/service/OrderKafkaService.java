package com.eswar.orderservice.kafka.service;

import com.eswar.orderservice.entity.EventEntity;
import com.eswar.orderservice.entity.OrderEntity;
import com.eswar.orderservice.kafka.constatnts.EventStatus;
import com.eswar.orderservice.kafka.constatnts.EventType;
import com.eswar.orderservice.kafka.event.OrderCreatedEvent;
import com.eswar.orderservice.kafka.event.OrderItemEvent;
import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import com.eswar.orderservice.kafka.producer.OrderEventProducer;
import com.eswar.orderservice.repository.IEventRepository;
import com.eswar.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderKafkaService {


    private final OrderEventProducer orderEventProducer;



    public void sendOrderCreatedEvent(@NonNull OrderEntity order, BigDecimal totalAmount) {

        Set<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(
                        i.getId().getProductId(),
                        i.getQuantity()
                )).collect(Collectors.toSet());

        OrderCreatedEvent  event = new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                order.getCustomerId(),
                totalAmount,
                items
        );

        orderEventProducer.sendOrderCreatedEvent(event);
    }
}