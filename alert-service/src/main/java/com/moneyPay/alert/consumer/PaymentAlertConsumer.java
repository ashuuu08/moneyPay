package com.moneyPay.alert.consumer;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens to payment.failed and payment.initiated
 * events for anomaly detection and ops alerting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentAlertConsumer {

    private final AlertService alertService;

    /**
     * Triggered on every payment failure — alerts ops immediately.
     */
    @KafkaListener(
        topics = "payment.failed",
        groupId = "alert-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentFailed(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.warn("Alert consumer: payment FAILED paymentId={} topic={} partition={} offset={}",
                event.getPaymentId(), topic, partition, offset);
        alertService.alertPaymentFailure(event);
    }
}
