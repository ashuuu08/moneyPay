package com.moneyPay.notification.service;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.notification.domain.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Orchestrates notification dispatch for payment lifecycle events.
 *
 * <p>Deduplication: uses Redis to ensure each (paymentId + eventType) pair
 * is only notified once, even if the Kafka message is redelivered.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String PREFIX = "notif:sent:";
    private static final Duration TTL   = Duration.ofDays(7);

    private final EmailNotificationService emailService;
    private final SmsNotificationService   smsService;
    private final RedisTemplate<String, String> redis;

    @Value("${notification.from-email:noreply@moneypay.io}")
    private String fromEmail;

    /**
     * Builds and dispatches notifications for a payment event.
     * Idempotent — duplicate events are silently ignored.
     */
    public void notifyPaymentEvent(PaymentEvent event) {
        String dedupeKey = PREFIX + event.getPaymentId() + ":" + event.getEventType();

        if (Boolean.TRUE.equals(redis.hasKey(dedupeKey))) {
            log.debug("Notification already sent — skipping. paymentId={} eventType={}",
                    event.getPaymentId(), event.getEventType());
            return;
        }

        NotificationMessage msg = buildMessage(event);
        emailService.send(msg);
        smsService.send(msg);

        redis.opsForValue().set(dedupeKey, "1", TTL);
        log.info("Notification dispatched paymentId={} eventType={}", event.getPaymentId(), event.getEventType());
    }

    private NotificationMessage buildMessage(PaymentEvent event) {
        boolean success = "SUCCESS".equals(event.getEventType());

        String subject = success
                ? "Payment Successful - Order #" + event.getOrderId()
                : "Payment Failed - Order #" + event.getOrderId();

        String body = success
                ? String.format(
                        "Your payment of %s %s for order %s was successful.\n" +
                        "Payment ID: %s\nThank you for using MoneyPay!",
                        event.getAmount(), event.getCurrency(), event.getOrderId(), event.getPaymentId())
                : String.format(
                        "Your payment of %s %s for order %s could not be processed.\n" +
                        "Payment ID: %s\nPlease try again or contact support.",
                        event.getAmount(), event.getCurrency(), event.getOrderId(), event.getPaymentId());

        // In production, recipient comes from a user profile service lookup
        // Here we use userId as a placeholder identifier
        return NotificationMessage.builder()
                .recipient(event.getUserId() + "@placeholder.com")
                .subject(subject)
                .body(body)
                .paymentId(event.getPaymentId())
                .eventType(event.getEventType())
                .build();
    }
}
