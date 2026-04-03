package com.eswar.paymentservice.entity;

import com.eswar.paymentservice.audit.AbstractAuditingEntity;
import com.eswar.paymentservice.constatns.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments",
indexes = {
@Index(name = "idx_razorpay_payment_id", columnList = "razorpayPaymentId"),
@Index(name = "idx_razorpay_order_id", columnList = "razorpayOrderId"),
@Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_user_status", columnList = "userId,status")
    })

@Getter
@Setter
@ToString(exclude = {"razorpayPaymentId", "razorpayOrderId"})
public class PaymentEntity  extends AbstractAuditingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // ✅ Razorpay Order ID
    @Column(unique = true, nullable = false,length = 50)
    private String razorpayOrderId;

    // ✅ Razorpay Payment ID
    @Column(unique = true, nullable = false,length = 50)
    private String razorpayPaymentId;

    @Version
    private Long version;
}
