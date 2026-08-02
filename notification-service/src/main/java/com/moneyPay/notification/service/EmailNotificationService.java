package com.moneyPay.notification.service;

import com.moneyPay.notification.domain.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Dispatches notification emails via Spring Mail (SMTP).
 * In local dev this logs to console if no SMTP server is configured.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public void send(NotificationMessage msg) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(msg.getRecipient());
            mail.setSubject(msg.getSubject());
            mail.setText(msg.getBody());
            mailSender.send(mail);
            log.info("Email sent to={} paymentId={}", msg.getRecipient(), msg.getPaymentId());
        } catch (Exception e) {
            log.warn("Email dispatch failed to={} paymentId={} reason={}",
                    msg.getRecipient(), msg.getPaymentId(), e.getMessage());
        }
    }
}
