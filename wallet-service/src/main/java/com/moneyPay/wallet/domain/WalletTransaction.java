package com.moneyPay.wallet.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Immutable record of a single debit or credit operation on a wallet.
 */
@Entity
@Table(
    name = "wallet_transactions",
    indexes = {
        @Index(name = "idx_wallet_tx_wallet_id",   columnList = "wallet_id"),
        @Index(name = "idx_wallet_tx_reference_id", columnList = "reference_id"),
        @Index(name = "idx_wallet_tx_created_at",   columnList = "created_at DESC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** paymentId that triggered this transaction */
    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(length = 255)
    private String description;

    /** Balance after this transaction was applied */
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum TransactionType {
        CREDIT, DEBIT
    }
}
