package com.eswar.paymentservice.repository;

import com.eswar.paymentservice.entity.EventEntity;
import com.eswar.paymentservice.kafka.constants.EventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IEventRepository extends JpaRepository<EventEntity, UUID> {
    // Spring Proxy generates:
    // SELECT * FROM inventory_events
    // WHERE order_id = ? AND event_type = ?
    // ORDER BY created_at DESC LIMIT 1
    Optional<EventEntity> findFirstByOrderIdAndEventTypeOrderByCreatedAtDesc(
            UUID orderId,
            EventType eventType
    );
}
