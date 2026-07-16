package com.moneyPay.alert;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// TODO Phase 3: Consume failure/anomaly Kafka events (payment.failed, high-error-rate alerts),
//  notify ops teams via Slack webhooks, PagerDuty, and email.
@SpringBootApplication
public class AlertServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AlertServiceApplication.class, args); }
}
