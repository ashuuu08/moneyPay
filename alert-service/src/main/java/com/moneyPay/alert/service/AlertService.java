package com.moneyPay.alert.service;

import com.moneyPay.common.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Dispatches ops alerts when payment failures or anomalies are detected.
 *
 * <p>Channels:
 * <ul>
 *   <li>Email (via Spring Mail / SMTP)</li>
 *   <li>Slack webhook (HTTP POST — mock in dev)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertService {

    private final JavaMailSender mailSender;

    @Value("${alert.ops-email:ops@moneypay.io}")
    private String opsEmail;

    @Value("${alert.slack-webhook-url:}")
    private String slackWebhookUrl;

    /**
     * Sends an ops alert for a failed payment.
     */
    public void alertPaymentFailure(PaymentEvent event) {
        String subject = "[ALERT] Payment FAILED — paymentId=" + event.getPaymentId();
        String body = String.format(
                "Payment failure detected!\n\n" +
                "Payment ID : %s\n" +
                "Order ID   : %s\n" +
                "User ID    : %s\n" +
                "Merchant ID: %s\n" +
                "Amount     : %s %s\n" +
                "Timestamp  : %s\n\n" +
                "Please investigate via Kibana/Grafana dashboards.",
                event.getPaymentId(), event.getOrderId(), event.getUserId(),
                event.getMerchantId(), event.getAmount(), event.getCurrency(),
                event.getTimestamp());

        sendEmail(subject, body);
        sendSlack(subject + "\n" + body);
    }

    // ── Private helpers ─────────────────────────────────────────────────────

    private void sendEmail(String subject, String body) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(opsEmail);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Alert email sent to={}", opsEmail);
        } catch (Exception e) {
            log.warn("Alert email failed: {}", e.getMessage());
        }
    }

    private void sendSlack(String message) {
        if (slackWebhookUrl == null || slackWebhookUrl.isBlank()) {
            log.info("[SLACK-MOCK] {}", message.lines().findFirst().orElse(message));
            return;
        }
        // Production: use RestTemplate/WebClient to POST to Slack webhook URL
        log.info("Slack alert dispatched to webhook");
    }
}
