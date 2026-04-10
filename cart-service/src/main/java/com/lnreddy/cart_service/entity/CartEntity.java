package com.lnreddy.cart_service.entity;

import com.lnreddy.cart_service.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true,callSuper = false)
public class CartEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String userId;

    // Orchestrates the items; orphanRemoval ensures that if an item
    // is removed from the Set, it is deleted from the DB.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItemEntity> items = new HashSet<>();

    // Helper method to keep both sides of the relationship in sync
    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }
}
