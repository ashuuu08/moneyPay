package com.moneyPay.ledger.consumer;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.ledger.service.LedgerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka consumer that listens to {@code payment.success} and triggers
 * double-entry ledger posting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(
        topics = "payment.success",
        groupId = "ledger-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentSuccess(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Ledger consumer received paymentId={} from topic={} partition={} offset={}",
                event.getPaymentId(), topic, partition, offset);

        ledgerService.postDoubleEntry(
                UUID.fromString(event.getPaymentId()),
                UUID.fromString(event.getUserId()),
                UUID.fromString(event.getMerchantId()),
                event.getAmount(),
                event.getCurrency(),
                event.getOrderId()
        );
    }
}
