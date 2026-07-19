package com.moneyPay.payment.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full payment snapshot returned after initiation or status lookup.
 */
@Data
@Builder
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String userId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String provider;
    private String providerRef;
    private String idempotencyKey;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
