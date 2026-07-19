package com.moneyPay.payment.controller;

import com.moneyPay.common.dto.ApiResponse;
import com.moneyPay.payment.dto.PaymentRequest;
import com.moneyPay.payment.dto.PaymentResponse;
import com.moneyPay.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for payment initiation and status lookup.
 * All routes are protected by the JWT filter at the API Gateway.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment initiation and status APIs")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Initiate a new payment.
     * Supply an {@code idempotencyKey} to safely retry without duplicate charges.
     */
    @Operation(summary = "Initiate a payment")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Payment processed", response));
    }

    /**
     * Retrieve a payment by its internal ID.
     */
    @Operation(summary = "Get payment status by ID")
    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @PathVariable UUID paymentId) {

        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
