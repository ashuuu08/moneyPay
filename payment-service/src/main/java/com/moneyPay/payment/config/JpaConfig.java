package com.moneyPay.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Enables JPA auditing so @CreatedDate / @LastModifiedDate are populated automatically.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
