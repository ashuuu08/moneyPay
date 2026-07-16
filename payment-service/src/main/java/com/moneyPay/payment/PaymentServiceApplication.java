package com.moneyPay.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// TODO Phase 2: Implement payment initiation API, idempotency key handling,
//  Kafka event publishing (payment.initiated, payment.success, payment.failed),
//  and mock sandbox payment processor integration (Razorpay/Stripe test mode).
@SpringBootApplication
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
