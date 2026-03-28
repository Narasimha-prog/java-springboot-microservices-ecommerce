package com.eswar.orderservice.entity;




import com.eswar.orderservice.audit.AbstractAuditingEntity;
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
public class OrderEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // reference to User Service
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    //status of this order
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // payment reference from Payment Service
    @Column(name = "payment_reference")
    private String paymentReference;

    //items to order right now
    @OneToMany(mappedBy = "id.order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderedItemEntity> items = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "order_processed_events", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "event_id")
    private List<UUID> processedEventIds = new ArrayList<>();


}
