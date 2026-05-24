package com.eswar.productservice.entity;


import com.eswar.productservice.constatnts.EventStatus;
import com.eswar.productservice.constatnts.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_events",
        indexes = {
                // Fixed: Column names now point to the actual fields in this class
                @Index(name = "idx_outbox_id", columnList = "id"),
                @Index(name = "idx_outbox_status", columnList = "status"),
                @Index(name = "idx_outbox_created_at", columnList = "created_at") // Useful for findByStatusOrderByCreatedAtAsc
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ProductCreatedEventEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(nullable = false)
    @ToString.Include
    private String topic;

    @Column(name = "partition_key")
    @ToString.Include
    private String partitionKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    @ToString.Include
    private String payload; // Stores the serialized ProductCreatedEvent JSON string

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventStatus status; // PENDING, PROCESSED, FAILED

    @Column(name = "created_at", nullable = false)
    @ToString.Include
    private LocalDateTime createdAt;
}
