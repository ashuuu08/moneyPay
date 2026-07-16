package com.moneyPay.common.exception;

import com.moneyPay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralized exception handler for all REST controllers.
 * Converts exceptions into the standard {@link ApiResponse} envelope.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MoneyPayException.class)
    public ResponseEntity<ApiResponse<Void>> handleMoneyPayException(MoneyPayException ex) {
        log.error("MoneyPayException [{}]: {}", ex.getErrorCode(), ex.getMessage(), ex);
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", details);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error("Validation failed: " + details, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
    }
}
