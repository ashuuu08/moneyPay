package com.moneyPay.alert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Alert Service — monitors payment failures and anomalies,
 * notifies ops team via email/Slack/PagerDuty (Phase 3).
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.alert", "com.moneyPay.common"})
@EnableDiscoveryClient
@EnableKafka
public class AlertServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertServiceApplication.class, args);
    }
}
