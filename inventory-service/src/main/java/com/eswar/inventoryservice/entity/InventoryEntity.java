package com.eswar.inventoryservice.entity;


import com.eswar.inventoryservice.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEntity extends BaseEntity {

    @Id
    private UUID productId;

    @Column(nullable = false)
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    private Long version;
}
