package com.moneyPay.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis-backed idempotency guard for payment initiation.
 *
 * <p>Flow:
 * <ol>
 *   <li>Before processing, call {@link #getStoredResponse(String)}.
 *   <li>If present, return the cached JSON response immediately — no duplicate charge.
 *   <li>If absent, process the payment, then call {@link #storeResponse(String, String)}.
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String PREFIX = "idempotency:payment:";
    private static final Duration TTL  = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Returns the cached response body for the given key, if it exists.
     */
    public Optional<String> getStoredResponse(String idempotencyKey) {
        String value = redisTemplate.opsForValue().get(PREFIX + idempotencyKey);
        if (value != null) {
            log.debug("Idempotency cache hit for key={}", idempotencyKey);
        }
        return Optional.ofNullable(value);
    }

    /**
     * Persists the serialised response for future idempotent replay.
     * TTL = 7 days (same window as refresh token).
     */
    public void storeResponse(String idempotencyKey, String responseJson) {
        redisTemplate.opsForValue().set(PREFIX + idempotencyKey, responseJson, TTL);
        log.debug("Idempotency response stored for key={}", idempotencyKey);
    }
}
