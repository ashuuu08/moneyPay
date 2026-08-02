package com.moneyPay.notification.service;

import com.moneyPay.notification.domain.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Mock SMS dispatcher — logs to console in local dev.
 * In production, replace with Twilio / AWS SNS SDK.
 */
@Slf4j
@Service
public class SmsNotificationService {

    public void send(NotificationMessage msg) {
        // Production: integrate Twilio/AWS SNS here
        log.info("[SMS-MOCK] To={} | Subject={} | paymentId={}",
                msg.getRecipient(), msg.getSubject(), msg.getPaymentId());
    }
}
