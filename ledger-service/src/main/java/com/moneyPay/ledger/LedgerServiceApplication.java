package com.moneyPay.ledger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// TODO Phase 2: Consume payment/wallet Kafka events, record double-entry ledger entries,
//  maintain audit trail with who/what/when for all financial mutations.
@SpringBootApplication
public class LedgerServiceApplication {
    public static void main(String[] args) { SpringApplication.run(LedgerServiceApplication.class, args); }
}
