package com.moneyPay.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service — dispatches email/SMS/webhook alerts on payment events (Phase 3).
 * Consumes: payment.success, payment.failed from Kafka.
 * Uses Redis to deduplicate notification delivery.
 */
@SpringBootApplication(scanBasePackages = {"com.moneyPay.notification", "com.moneyPay.common"})
@EnableDiscoveryClient
@EnableKafka
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}



