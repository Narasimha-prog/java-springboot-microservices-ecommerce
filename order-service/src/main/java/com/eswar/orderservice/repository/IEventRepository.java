package com.eswar.orderservice.repository;

import com.eswar.orderservice.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IEventRepository extends JpaRepository<EventEntity, UUID> {
}
