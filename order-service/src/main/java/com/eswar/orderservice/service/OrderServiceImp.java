package com.eswar.orderservice.service;

import com.eswar.orderservice.constants.OrderStatus;
import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderItemDto;
import com.eswar.orderservice.dto.OrderResponseDto;
import com.eswar.orderservice.entity.OrderEntity;
import com.eswar.orderservice.entity.OrderedItemEntity;
import com.eswar.orderservice.entity.OrderedItemId;
import com.eswar.orderservice.kafka.event.OrderCreatedEvent;
import com.eswar.orderservice.kafka.event.OrderItemEvent;
import com.eswar.orderservice.kafka.producer.OrderEventProducer;
import com.eswar.orderservice.mapper.IOrderMapper;
import com.eswar.orderservice.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderServiceImp implements IOrderService{

    private final IOrderRepository orderRepository;
    private final IOrderMapper mapper;
    private final OrderEventProducer orderEventProducer;

    public OrderResponseDto createOrder(OrderDto dto) {

        OrderEntity order = mapper.toEntity(dto);

        order.setStatus(OrderStatus.CREATED);

        Set<OrderedItemEntity> items = new HashSet<>();

        for (OrderItemDto itemDto : dto.items()) {

            OrderedItemId id = new OrderedItemId();
            id.setOrder(order);
            id.setProductId(itemDto.productId());

            OrderedItemEntity item = new OrderedItemEntity();
            item.setId(id);
            item.setQuantity(itemDto.quantity());

            items.add(item);
        }

        order.setItems(items);

        OrderEntity saved = orderRepository.save(order);

        publishOrderCreatedEvent(saved);

        return mapper.toResponse(saved);
    }

    private void publishOrderCreatedEvent(OrderEntity order) {

        List<OrderItemEvent> items = order.getItems().stream()
                .map(i -> new OrderItemEvent(
                        i.getId().getProductId(),
                        i.getQuantity()
                ))
                .toList();

        OrderCreatedEvent event =
                new OrderCreatedEvent(
                        order.getId(),
                        order.getCustomerId(),
                        BigDecimal.ZERO, // optional calculation
                        items
                );

        orderEventProducer.sendOrderCreatedEvent(event);
    }
}
