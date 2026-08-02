package com.moneyPay.merchant.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * API key issued to a merchant for programmatic access to MoneyPay APIs.
 * Keys are hashed before storage (never stored in plain text).
 */
@Entity
@Table(
    name = "merchant_api_keys",
    indexes = {
        @Index(name = "idx_api_key_merchant_id", columnList = "merchant_id"),
        @Index(name = "idx_api_key_prefix",       columnList = "key_prefix", unique = true)
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MerchantApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    /**
     * First 8 chars of the plain-text key (safe to display, allows lookup).
     * Example: "mpk_live_"
     */
    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    /** BCrypt hash of the full API key — never return this via API */
    @Column(name = "key_hash", nullable = false)
    private String keyHash;

    /** Human-readable label set by the merchant */
    @Column(length = 100)
    private String label;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;
}
