package com.moneyPay.merchant.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Merchant entity — stores merchant profile, KYC status, and business details.
 */
@Entity
@Table(
    name = "merchants",
    indexes = {
        @Index(name = "idx_merchant_user_id",  columnList = "user_id", unique = true),
        @Index(name = "idx_merchant_kyc_status", columnList = "kyc_status")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** References the userId from auth-service */
    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "business_email", nullable = false, unique = true, length = 150)
    private String businessEmail;

    @Column(name = "business_phone", length = 20)
    private String businessPhone;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "pan", length = 10)
    private String pan;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MerchantStatus status = MerchantStatus.ACTIVE;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum KycStatus {
        PENDING, UNDER_REVIEW, APPROVED, REJECTED
    }

    public enum MerchantStatus {
        ACTIVE, SUSPENDED, CLOSED
    }
}
