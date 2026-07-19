package com.moneyPay.wallet.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a user's wallet — one wallet per user per currency.
 */
@Entity
@Table(
    name = "wallets",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "currency"})
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /** ISO 4217 currency code, e.g. "INR" */
    @Column(nullable = false, length = 10)
    private String currency;

    /**
     * Current balance — updated atomically via optimistic locking.
     * Never goes below zero (enforced at service layer + DB constraint).
     */
    @Column(nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    /** Optimistic lock version — prevents lost-update race conditions */
    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
