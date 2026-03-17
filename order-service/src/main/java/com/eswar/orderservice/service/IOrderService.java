package com.eswar.orderservice.service;

import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderResponseDto;


public interface IOrderService {
    OrderResponseDto createOrder(OrderDto dto);

}
