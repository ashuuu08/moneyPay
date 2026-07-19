package com.moneyPay.wallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Wallet Service — manages user wallet balances.
 * Consumes payment.success / payment.failed Kafka events (Phase 2).
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.wallet", "com.moneyPay.common"})
@EnableDiscoveryClient
@EnableKafka
public class WalletServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}
