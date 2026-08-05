package com.moneyPay.gateway.controller;

import com.moneyPay.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger logger = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/{serviceName}")
    public Mono<ResponseEntity<ApiResponse<Void>>> serviceFallback(@PathVariable String serviceName) {
        logger.warn("Circuit Breaker triggered for service: {}", serviceName);
        
        ApiResponse<Void> response = ApiResponse.error(
                serviceName + " is currently unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}
