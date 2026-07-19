package com.moneyPay.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Shared Kafka event published by payment-service and consumed by
 * wallet-service, ledger-service, and notification-service.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentEvent {

    /** Unique ID of the payment record */
    private String paymentId;

    /** Caller-supplied order identifier */
    private String orderId;

    /** The paying user */
    private String userId;

    /** The receiving merchant */
    private String merchantId;

    /** Amount transacted */
    private BigDecimal amount;

    /** ISO 4217 currency code, e.g. "INR" */
    private String currency;

    /**
     * Lifecycle discriminator.
     * Values: INITIATED | SUCCESS | FAILED | REFUNDED
     */
    private String eventType;

    /** UTC timestamp when this event was emitted */
    @Builder.Default
    private Instant timestamp = Instant.now();
}
