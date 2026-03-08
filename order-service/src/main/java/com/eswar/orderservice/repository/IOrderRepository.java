package com.eswar.orderservice.repository;

import com.eswar.orderservice.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IOrderRepository  extends JpaRepository<OrderEntity, UUID> {
}
