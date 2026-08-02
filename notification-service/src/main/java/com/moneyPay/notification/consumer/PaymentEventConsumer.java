package com.moneyPay.notification.consumer;

import com.moneyPay.common.event.PaymentEvent;
import com.moneyPay.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for payment lifecycle events.
 * Listens to payment.success and payment.failed topics.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics = {"payment.success", "payment.failed"},
        groupId = "notification-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentEvent(
            @Payload PaymentEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Notification consumer received eventType={} paymentId={} topic={} partition={} offset={}",
                event.getEventType(), event.getPaymentId(), topic, partition, offset);

        notificationService.notifyPaymentEvent(event);
    }
}
