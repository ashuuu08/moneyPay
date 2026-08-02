package com.moneyPay.notification.domain;

import lombok.Builder;
import lombok.Data;

/**
 * Internal notification request — built from a PaymentEvent and routed
 * to the appropriate channel dispatcher.
 */
@Data
@Builder
public class NotificationMessage {

    /** Recipient identifier (email address, phone number, or webhook URL) */
    private String recipient;

    private NotificationChannel channel;

    /** Short subject/title for email or SMS */
    private String subject;

    /** Full notification body */
    private String body;

    /** Original payment ID for idempotency deduplication */
    private String paymentId;

    /** Event type that triggered this notification: SUCCESS | FAILED */
    private String eventType;
}
