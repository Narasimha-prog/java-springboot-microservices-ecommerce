package com.eswar.inventoryservice.entity;

import com.eswar.inventoryservice.audit.BaseEntity;
import com.eswar.inventoryservice.kafka.constants.EventStatus;
import com.eswar.inventoryservice.kafka.constants.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "inventory_events",
        indexes = {
                @Index(name = "idx_inventary_ievent_id", columnList = "eventId"),
                @Index(name = "idx_inventory_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EventEntity extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID eventId; // 🔥 comes from Kafka

    @Column(nullable = false)
    @ToString.Include
    private UUID orderId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private EventType eventType; // INVENTORY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private EventStatus status; // RECEIVED / PROCESSED / FAILED

    @Column(columnDefinition = "TEXT")
    @ToString.Include
    private String payload; // optional (debugging)

    @ToString.Include
    private String errorMessage;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "inventory_event_trace_ids", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "trace_id")
    private Set<UUID> traceIds = new HashSet<>(); // store all traceIds
}
