package com.moneyPay.ledger.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A single line in the double-entry ledger.
 *
 * <p>Each payment.success event produces exactly two entries:
 * <ul>
 *   <li>DEBIT  — user account (money leaving the buyer)</li>
 *   <li>CREDIT — merchant account (money arriving at the seller)</li>
 * </ul>
 * The sum of all DEBIT entries equals the sum of all CREDIT entries (accounting invariant).
 */
@Entity
@Table(
    name = "ledger_entries",
    indexes = {
        @Index(name = "idx_ledger_payment_id",  columnList = "payment_id"),
        @Index(name = "idx_ledger_account_id",  columnList = "account_id"),
        @Index(name = "idx_ledger_posted_at",   columnList = "posted_at DESC")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Payment that triggered this entry */
    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    /** Either userId (DEBIT) or merchantId (CREDIT) */
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    /** ISO 4217 currency code */
    @Column(nullable = false, length = 10)
    private String currency;

    @Column(length = 255)
    private String description;

    @CreatedDate
    @Column(name = "posted_at", nullable = false, updatable = false)
    private Instant postedAt;

    public enum EntryType {
        DEBIT, CREDIT
    }
}
