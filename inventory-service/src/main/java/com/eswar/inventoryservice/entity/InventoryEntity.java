package com.eswar.inventoryservice.entity;


import com.eswar.inventoryservice.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
@Entity
@Table(name = "inventory",
indexes = {
               @Index(name = "idx_product_id", columnList = "productId")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class InventoryEntity extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID productId;

    @Column(nullable = false)
    @Builder.Default
    @ToString.Include
    private Integer availableQuantity = 0;

    @Column(nullable = false)
    @Builder.Default
    @ToString.Include
    private Integer reservedQuantity = 0;

    @Version
    private Long version=0L;
}
