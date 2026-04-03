package com.eswar.orderservice.entity;

import com.eswar.orderservice.audit.BaseEntity;
import com.eswar.orderservice.kafka.constatnts.EventStatus;
import com.eswar.orderservice.kafka.constatnts.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "order_events",
        indexes = {
                @Index(name = "idx_event_id", columnList = "eventId"),
                @Index(name = "idx_status", columnList = "status")
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
    private UUID eventId;

    @Column(nullable = false)
    @ToString.Include
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private EventStatus status;

    @Column(columnDefinition = "TEXT")
    @ToString.Include
    private String payload;

    @ToString.Include
    private String errorMessage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_event_trace_ids", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "trace_id")
    private Set<UUID> traceIds = new HashSet<>(); // store all traceIds
}