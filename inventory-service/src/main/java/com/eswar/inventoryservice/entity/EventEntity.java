package com.eswar.inventoryservice.entity;

import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "inventory_events",
        indexes = {
                @Index(name = "idx_event_id", columnList = "eventId"),
                @Index(name = "idx_status", columnList = "status")
        })
@Getter
@Setter
@EntityListeners(Abstr)
public class InventoryEventEntity {

    @Id
    private UUID eventId; // 🔥 comes from Kafka

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType=EventType.INVENTORY; // INVENTORY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status; // RECEIVED / PROCESSED / FAILED

    @Lob
    private String payload; // optional (debugging)

    private String errorMessage;
}
