package com.moneyPay.payment.service;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.payment.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Wraps KafkaTemplate and publishes {@link PaymentEvent} to the correct topic
 * based on the event's {@code eventType} field.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventPublisher {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    /**
     * Publishes the event asynchronously.
     * The payment ID is used as the Kafka message key so all events for
     * the same payment land on the same partition (ordering guarantee).
     */
    public void publish(PaymentEvent event) {
        String topic = switch (event.getEventType()) {
            case "INITIATED" -> KafkaConfig.TOPIC_PAYMENT_INITIATED;
            case "SUCCESS"   -> KafkaConfig.TOPIC_PAYMENT_SUCCESS;
            case "FAILED"    -> KafkaConfig.TOPIC_PAYMENT_FAILED;
            default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
        };

        CompletableFuture<SendResult<String, PaymentEvent>> future =
                kafkaTemplate.send(topic, event.getPaymentId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published {} event paymentId={} to topic={} partition={} offset={}",
                        event.getEventType(), event.getPaymentId(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish {} event paymentId={}: {}",
                        event.getEventType(), event.getPaymentId(), ex.getMessage(), ex);
            }
        });
    }
}
