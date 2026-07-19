package com.moneyPay.ledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Ledger Service — double-entry bookkeeping for all financial mutations (Phase 2).
 * Consumes payment.success events and posts paired DEBIT/CREDIT ledger entries.
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.ledger", "com.moneyPay.common"})
@EnableDiscoveryClient
@EnableKafka
public class LedgerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(LedgerServiceApplication.class, args);
    }
}
