package com.moneyPay.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Inbound DTO for POST /api/v1/payments
 */
@Data
public class PaymentRequest {

    @NotBlank(message = "orderId is required")
    @Size(max = 100)
    private String orderId;

    @NotBlank(message = "userId is required")
    private String userId;

    @NotBlank(message = "merchantId is required")
    private String merchantId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    @Digits(integer = 15, fraction = 4)
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Size(min = 3, max = 10)
    private String currency;

    /**
     * Client-supplied idempotency key — must be unique per payment attempt.
     * Re-submitting with the same key returns the original response without
     * creating a duplicate charge.
     */
    @NotBlank(message = "idempotencyKey is required")
    @Size(max = 100)
    private String idempotencyKey;

    @Size(max = 255)
    private String description;
}
