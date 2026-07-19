package com.moneyPay.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.common.exception.MoneyPayException;
import com.moneyPay.payment.domain.Payment;
import com.moneyPay.payment.dto.PaymentRequest;
import com.moneyPay.payment.dto.PaymentResponse;
import com.moneyPay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Core orchestrator for the payment initiation flow:
 * <ol>
 *   <li>Idempotency check (Redis) — return cached response if key already seen</li>
 *   <li>Persist payment with status {@code INITIATED}</li>
 *   <li>Publish {@code payment.initiated} Kafka event</li>
 *   <li>Call mock gateway</li>
 *   <li>Update status to {@code SUCCESS} or {@code FAILED}</li>
 *   <li>Publish {@code payment.success} or {@code payment.failed} event</li>
 *   <li>Cache the response for idempotent replay</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository         paymentRepository;
    private final IdempotencyService        idempotencyService;
    private final MockPaymentGatewayService gatewayService;
    private final PaymentEventPublisher     eventPublisher;
    private final ObjectMapper              objectMapper;

    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // 1. Idempotency check
        var cachedJson = idempotencyService.getStoredResponse(request.getIdempotencyKey());
        if (cachedJson.isPresent()) {
            log.info("Idempotent replay for key={}", request.getIdempotencyKey());
            return deserialize(cachedJson.get());
        }

        // 2. Persist with INITIATED status
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(UUID.fromString(request.getUserId()))
                .merchantId(UUID.fromString(request.getMerchantId()))
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .idempotencyKey(request.getIdempotencyKey())
                .description(request.getDescription())
                .provider("MOCK_GATEWAY")
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment INITIATED id={} orderId={}", payment.getId(), payment.getOrderId());

        // 3. Publish initiated event
        eventPublisher.publish(buildEvent(payment, "INITIATED"));

        // 4. Charge via mock gateway
        MockPaymentGatewayService.GatewayResult result =
                gatewayService.charge(payment.getOrderId(), payment.getAmount(), payment.getCurrency());

        // 5. Update status
        if (result.success()) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setProviderRef(result.providerRef());
            log.info("Payment SUCCESS id={} providerRef={}", payment.getId(), result.providerRef());
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            log.warn("Payment FAILED id={} reason={}", payment.getId(), result.errorMessage());
        }
        payment = paymentRepository.save(payment);

        // 6. Publish success / failed event
        String eventType = result.success() ? "SUCCESS" : "FAILED";
        eventPublisher.publish(buildEvent(payment, eventType));

        // 7. Build response, cache, and return
        PaymentResponse response = toResponse(payment);
        idempotencyService.storeResponse(request.getIdempotencyKey(), serialize(response));
        return response;
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new MoneyPayException(
                        "Payment not found", HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND"));
        return toResponse(payment);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private PaymentEvent buildEvent(Payment payment, String eventType) {
        return PaymentEvent.builder()
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId().toString())
                .merchantId(payment.getMerchantId().toString())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .eventType(eventType)
                .build();
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId().toString())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId().toString())
                .merchantId(payment.getMerchantId().toString())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .provider(payment.getProvider())
                .providerRef(payment.getProviderRef())
                .idempotencyKey(payment.getIdempotencyKey())
                .description(payment.getDescription())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private String serialize(PaymentResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new MoneyPayException("Serialization error", HttpStatus.INTERNAL_SERVER_ERROR, "SERIALIZATION_ERROR");
        }
    }

    private PaymentResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, PaymentResponse.class);
        } catch (JsonProcessingException e) {
            throw new MoneyPayException("Deserialization error", HttpStatus.INTERNAL_SERVER_ERROR, "DESERIALIZATION_ERROR");
        }
    }
}
