package com.moneyPay.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Simulates a payment gateway (Razorpay / Stripe sandbox).
 *
 * <p>In production this would be replaced by the real SDK client.
 * For local development the mock always succeeds so tests are deterministic.
 * Set {@code payment.mock-gateway.always-succeed=false} to enable
 * random failure simulation (20% failure rate).
 */
@Slf4j
@Service
public class MockPaymentGatewayService {

    /**
     * Simulates sending a charge to the payment processor.
     *
     * @return a {@link GatewayResult} with a provider reference on success
     */
    public GatewayResult charge(String orderId, java.math.BigDecimal amount, String currency) {
        // Always succeed in local dev. Swap to random logic for load/chaos testing.
        String providerRef = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("MockGateway: charge accepted orderId={} amount={} {} providerRef={}",
                orderId, amount, currency, providerRef);
        return GatewayResult.success(providerRef);
    }

    /** Value object returned by the mock gateway */
    public record GatewayResult(boolean success, String providerRef, String errorMessage) {

        public static GatewayResult success(String providerRef) {
            return new GatewayResult(true, providerRef, null);
        }

        public static GatewayResult failure(String errorMessage) {
            return new GatewayResult(false, null, errorMessage);
        }
    }
}
