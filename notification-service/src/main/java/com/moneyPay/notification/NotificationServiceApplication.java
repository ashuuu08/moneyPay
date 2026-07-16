package com.moneyPay.notification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// TODO Phase 3: Consume payment.success/payment.failed Kafka events,
//  dispatch email/SMS/webhook notifications to merchants and customers.
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) { SpringApplication.run(NotificationServiceApplication.class, args); }
}
