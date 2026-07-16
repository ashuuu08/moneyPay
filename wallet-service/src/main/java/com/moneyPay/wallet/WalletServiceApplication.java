package com.moneyPay.wallet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// TODO Phase 2: Consume payment.initiated / payment.success / payment.failed Kafka events,
//  update wallet balances (credit/debit), publish balance.updated events.
@SpringBootApplication
public class WalletServiceApplication {
    public static void main(String[] args) { SpringApplication.run(WalletServiceApplication.class, args); }
}
