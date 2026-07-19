package com.moneyPay.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Payment Service — handles payment initiation, idempotency, mock gateway,
 * and Kafka event publishing (Phase 2).
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.payment", "com.moneyPay.common"})
@EnableDiscoveryClient
@EnableKafka
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
