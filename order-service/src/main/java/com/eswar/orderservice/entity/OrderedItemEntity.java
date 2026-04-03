package com.eswar.orderservice.entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ordered_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
public class OrderedItemEntity {

    //make order and product primary
    @EmbeddedId
    @EqualsAndHashCode.Include
    @ToString.Include
    private OrderedItemId id;

    @Column(nullable = false)
    @ToString.Include
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    @ToString.Include
    private BigDecimal price;
}
