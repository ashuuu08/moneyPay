package com.moneyPay.wallet.consumer;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka consumer for payment lifecycle events.
 *
 * <p>On {@code payment.success}:
 * <ul>
 *   <li>Credits the merchant's wallet (payment received)</li>
 *   <li>Debits the buyer's wallet if pre-funded (optional — no-op if wallet is empty)</li>
 * </ul>
 * On {@code payment.failed}: logs only, no balance change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final WalletService walletService;

    @KafkaListener(
        topics = {"payment.success", "payment.failed"},
        groupId = "wallet-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentEvent(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received {} from topic={} partition={} offset={} paymentId={}",
                event.getEventType(), topic, partition, offset, event.getPaymentId());

        if ("SUCCESS".equals(event.getEventType())) {
            handlePaymentSuccess(event);
        } else if ("FAILED".equals(event.getEventType())) {
            log.info("Payment failed — no wallet changes. paymentId={}", event.getPaymentId());
        }
    }

    private void handlePaymentSuccess(PaymentEvent event) {
        // Credit merchant wallet
        walletService.credit(
                UUID.fromString(event.getMerchantId()),
                event.getCurrency(),
                event.getAmount(),
                event.getPaymentId(),
                "Payment received: orderId=" + event.getOrderId()
        );

        // Debit buyer wallet (idempotent — silently skips if wallet is empty / not found)
        try {
            walletService.debit(
                    UUID.fromString(event.getUserId()),
                    event.getCurrency(),
                    event.getAmount(),
                    event.getPaymentId(),
                    "Payment sent: orderId=" + event.getOrderId()
            );
        } catch (Exception e) {
            // Non-critical: buyer wallet debit is best-effort for pre-funded wallets
            log.warn("Could not debit buyer wallet userId={} paymentId={}: {}",
                    event.getUserId(), event.getPaymentId(), e.getMessage());
        }
    }
}
