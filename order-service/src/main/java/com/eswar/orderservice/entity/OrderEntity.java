package com.eswar.orderservice.entity;




import com.eswar.orderservice.audit.BaseEntity;
import com.eswar.orderservice.constants.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true,callSuper = false)
public class OrderEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;

    // reference to User Service
    @Column(name = "customer_id", nullable = false)
    @ToString.Include
    private UUID customerId;

    //status of this order
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private OrderStatus status;

    // payment reference from Payment Service
    @Column(name = "payment_reference")
    @ToString.Include
    private String paymentReference;

    //items to order right now
    @OneToMany(mappedBy = "id.order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderedItemEntity> items = new LinkedHashSet<>();

    @Version
    private Long version;


}
