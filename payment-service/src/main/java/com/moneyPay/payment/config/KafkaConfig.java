package com.moneyPay.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares Kafka topics used by payment-service.
 * Topics are auto-created if they don't already exist.
 */
@Configuration
public class KafkaConfig {

    public static final String TOPIC_PAYMENT_INITIATED = "payment.initiated";
    public static final String TOPIC_PAYMENT_SUCCESS   = "payment.success";
    public static final String TOPIC_PAYMENT_FAILED    = "payment.failed";

    @Bean
    public NewTopic paymentInitiatedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_INITIATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_SUCCESS)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(TOPIC_PAYMENT_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
