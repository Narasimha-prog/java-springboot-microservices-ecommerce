package com.eswar.inventoryservice.repository;

import com.eswar.inventoryservice.entity.EventEntity;
import com.eswar.inventoryservice.kafka.constants.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IEventRepository extends JpaRepository<EventEntity, UUID> {

    Optional<EventEntity> findFirstByOrderIdAndEventTypeOrderByCreatedAtDesc(UUID orderId, EventType eventType);
}