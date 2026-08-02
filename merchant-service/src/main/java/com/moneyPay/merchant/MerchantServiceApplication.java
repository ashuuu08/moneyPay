package com.moneyPay.merchant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Merchant Service — merchant onboarding, KYC status, API key management (Phase 3).
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.merchant", "com.moneyPay.common"})
@EnableDiscoveryClient
public class MerchantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
