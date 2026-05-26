package com.srinu.chatbot_service.entity;


import com.srinu.chatbot_service.audit.BaseEntity;
import com.srinu.chatbot_service.kafka.constants.EventStatus;
import com.srinu.chatbot_service.kafka.constants.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chatbot_product_events",
        indexes = {
                @Index(name = "idx_chatbot_pevent_id", columnList = "eventId"),
                @Index(name = "idx_chatbot_pstatus", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductEventEntity extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID eventId; // Sourced directly from incoming Kafka payload record

    @Column(nullable = false)
    @ToString.Include
    private UUID productId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ToString.Include
    private EventType eventType; // CHATBOT_PRODUCT_SYNC / INVENTORY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private EventStatus status; // RECEIVED / PROCESSED / FAILED

    @Column(columnDefinition = "TEXT")
    @ToString.Include
    private String payload; // Stores raw message for debug tracking if required

    @ToString.Include
    private String errorMessage;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "chatbot_product_event_trace_ids", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "trace_id")
    @Builder.Default
    private Set<UUID> traceIds = new HashSet<>(); // Persists tracking hops for diagnostics safely
}
