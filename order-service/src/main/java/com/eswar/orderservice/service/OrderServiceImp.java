package com.eswar.orderservice.service;

import com.eswar.orderservice.constants.OrderStatus;
import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderItemDto;
import com.eswar.orderservice.dto.OrderResponseDto;
import com.eswar.orderservice.dto.PageResponse;
import com.eswar.orderservice.entity.OrderEntity;
import com.eswar.orderservice.entity.OrderedItemEntity;
import com.eswar.orderservice.entity.OrderedItemId;
import com.eswar.orderservice.exceptions.BusinessException;
import com.eswar.orderservice.exceptions.InvalidUserIdException;
import com.eswar.orderservice.exceptions.OrderNotFoundException;
import com.eswar.orderservice.grpc.client.GrpcProductServiceClient;
import com.eswar.orderservice.kafka.event.OrderCreatedEvent;
import com.eswar.orderservice.kafka.event.OrderItemEvent;
import com.eswar.orderservice.kafka.producer.OrderEventProducer;
import com.eswar.orderservice.mapper.IOrderMapper;
import com.eswar.orderservice.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImp implements IOrderService{

    private final IOrderRepository orderRepository;
    private final IOrderMapper mapper;
    private final OrderEventProducer orderEventProducer;
    private final GrpcProductServiceClient grpcProductServiceClient;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderDto dto, Principal principal) {

        //getting entity
        OrderEntity order = mapper.toEntity(dto);
        //set status
        order.setStatus(OrderStatus.CREATED);

        //ordered items
        List<OrderedItemEntity> items = new ArrayList<>();

        //total amount of order
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : dto.items()) {

            // 🔥 CALL gRPC PRODUCT SERVICE
            var product = grpcProductServiceClient.getProduct(itemDto.productId());

            BigDecimal price = BigDecimal.valueOf(product.getPrice());


            // Calculate total
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemDto.quantity()));

            //added to total amount
            totalAmount = totalAmount.add(itemTotal);

            OrderedItemId id = new OrderedItemId();
            id.setOrder(order);
            id.setProductId(itemDto.productId());

            OrderedItemEntity item = new OrderedItemEntity();
            item.setId(id);
            item.setQuantity(itemDto.quantity());
            item.setPrice(price);

          //calculate total items
            items.add(item);
        }

       //added total items to order
        order.setItems(items);

        try {
            order.setCustomerId(UUID.fromString(principal.getName()));
        } catch (Exception e) {
            throw new InvalidUserIdException("Invalid userId in token");
        }

        //save to db
        OrderEntity saved = orderRepository.save(order);

        //send event
        publishOrderCreatedEvent(saved,totalAmount);

        return mapper.toResponse(saved);
    }

    @Override
    public PageResponse<OrderResponseDto> getALlOrders(Pageable pageable) {
        //see all orders

        Page<OrderEntity> page=orderRepository.findAll(pageable);
        List<OrderResponseDto> content=page.getContent().stream().map(mapper::toResponse).toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    public OrderResponseDto getOrderById(String orderId) {
        UUID id;
        try {
            id = UUID.fromString(orderId);
        } catch (Exception e) {
            throw new OrderNotFoundException("Order ID is not valid UUID");
        }

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        return mapper.toResponse(order);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(String orderId, OrderDto orderDto) {


        UUID id;
        try {
            id = UUID.fromString(orderId);
        } catch (Exception e) {
            throw new OrderNotFoundException(" Order ID is not valid UUID");
        }

        OrderEntity existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        if (existing.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("BUSINESS_ERROR","ORDER_CANNOT_BE_UPDATED");
        }

        // Only allow updating items and recalculate total
        List<OrderedItemEntity> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : orderDto.items()) {
            var product = grpcProductServiceClient.getProduct(itemDto.productId());
            BigDecimal price = BigDecimal.valueOf(product.getPrice());

            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(itemDto.quantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderedItemId orderedItemId = new OrderedItemId();
            orderedItemId.setOrder(existing);
            orderedItemId.setProductId(itemDto.productId());

            OrderedItemEntity item = new OrderedItemEntity();
            item.setId(orderedItemId);
            item.setQuantity(itemDto.quantity());
            item.setPrice(price);

            items.add(item);
        }

        existing.setItems(items);
        existing.setStatus(OrderStatus.CONFIRMED);

        OrderEntity saved = orderRepository.save(existing);

        publishOrderCreatedEvent(saved, totalAmount); // optionally publish updated event

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {

        UUID id;
        try {
            id = UUID.fromString(orderId);
        } catch (Exception e) {
            throw new OrderNotFoundException("INVALID_ORDER_ID, Order ID is not valid UUID");
        }

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("ORDER_NOT_FOUND, Order not found"));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Optional: publish cancel event
    }


    @Override
    public boolean isOrderOwnedByUser(String orderId, String userId) {

        UUID id;
        try {
            id = UUID.fromString(orderId);
        } catch (Exception e) {
            return false;
        }

        return orderRepository.findById(id)
                .map(o -> o.getCustomerId().toString().equals(userId))
                .orElse(false);
    }

    @Override
    public PageResponse<OrderResponseDto> getOrdersByCustomerId(String customerId,Pageable pageable) {
        UUID id;
        try {
            id = UUID.fromString(customerId);
        } catch (Exception e) {
            throw new InvalidUserIdException("Invalid Customer Id");
        }

        Page<OrderEntity> page=orderRepository.findByCustomerId(id,pageable);
        List<OrderResponseDto> content=page.getContent().stream().map(mapper::toResponse).toList();

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }


    @Transactional
    public void updateOrderStatus(UUID orderId,UUID eventId, String eventType, String status, String paymentReference) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (order.getProcessedEventIds().contains(eventId)) {
            return; // Already processed
        }
        switch (eventType) {

            case "INVENTORY":
                if (status.equals("SUCCESS")) {
                    order.setStatus(OrderStatus.STOCK_RESERVED);
                } else {
                    order.setStatus(OrderStatus.FAILED);
                }
                break;

            case "PAYMENT":
                if (status.equals("SUCCESS")) {
                    order.setStatus(OrderStatus.CONFIRMED);
                    order.setPaymentReference(paymentReference); // ✅ important
                } else {
                    order.setStatus(OrderStatus.CANCELLED);
                }
                break;
        }

        orderRepository.save(order);
    }
    private void publishOrderCreatedEvent(@NonNull OrderEntity order, BigDecimal totalAmount) {

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
                        totalAmount, // ✅ now correct
                        items
                );

        orderEventProducer.sendOrderCreatedEvent(event);
    }


}
