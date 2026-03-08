package com.eswar.orderservice.mapper;

import com.eswar.orderservice.dto.OrderDto;
import com.eswar.orderservice.dto.OrderResponseDto;
import com.eswar.orderservice.entity.OrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

   OrderEntity toEntity(OrderDto orderDto);
   OrderResponseDto toResponse(OrderEntity orderEntity);
}
