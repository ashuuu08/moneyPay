package com.moneyPay.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Core payment record — persisted to Postgres by payment-service.
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_order_id",       columnList = "order_id"),
        @Index(name = "idx_payments_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_payments_user_id",         columnList = "user_id"),
        @Index(name = "idx_payments_merchant_id",     columnList = "merchant_id")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Caller-supplied order reference — should be unique per merchant */
    @Column(name = "order_id", nullable = false, length = 100)
    private String orderId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** ISO 4217 currency code, e.g. "INR" */
    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.INITIATED;

    /** Mock/real payment provider name, e.g. "MOCK_GATEWAY" */
    @Column(length = 50)
    private String provider;

    /** Provider's own transaction/reference id */
    @Column(name = "provider_ref", length = 100)
    private String providerRef;

    /** Client-supplied idempotency key — enforced unique at DB level */
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
    private String idempotencyKey;

    /** Optional human-readable description */
    @Column(length = 255)
    private String description;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum PaymentStatus {
        INITIATED, SUCCESS, FAILED, REFUNDED
    }
}
