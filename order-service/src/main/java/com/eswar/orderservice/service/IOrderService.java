package com.eswar.orderservice.service;

import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderResponseDto;
import com.eswar.orderservice.dto.PageResponse;
import com.eswar.orderservice.kafka.constatnts.EventStatus;
import com.eswar.orderservice.kafka.constatnts.EventType;
import com.eswar.orderservice.kafka.event.OrderStatusEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.Principal;
import java.util.List;
import java.util.UUID;


public interface IOrderService {
     void handleOrderStatusEvent(OrderStatusEvent event);
    OrderResponseDto createOrder(OrderDto dto, Principal principal);
    PageResponse<OrderResponseDto> getALlOrders(Pageable pageable);
    OrderResponseDto getOrderById(UUID orderId);
    OrderResponseDto updateOrder(String orderId, OrderDto orderDto);
    void cancelOrder(String orderId);
    boolean isOrderOwnedByUser(String orderId, String userId);
    PageResponse<OrderResponseDto> getOrdersByCustomerId(String customerId,Pageable pageable);
    void updateOrderStatus(UUID orderId, EventType eventType,
                           EventStatus status, String paymentReference);

}
