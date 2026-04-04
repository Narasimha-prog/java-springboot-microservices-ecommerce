package com.eswar.paymentservice.entity;


import com.eswar.paymentservice.kafka.constants.EventStatus;
import com.eswar.paymentservice.kafka.constants.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "payment_events",
        indexes = {
                @Index(name = "payment_idx_event_id", columnList = "eventId"),
                @Index(name = "payment_idx_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class EventEntity extends com.eswar.paymentservice.audit.BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID eventId; // 🔥 comes from Kafka

    @ToString.Include
    private UUID traceId;

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
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "payment_event_trace_ids", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "trace_id")
    private Set<UUID> traceIds = new HashSet<>(); // store all traceIds
}
