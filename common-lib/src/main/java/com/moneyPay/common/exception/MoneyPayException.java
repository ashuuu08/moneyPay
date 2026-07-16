package com.moneyPay.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base application exception carrying an HTTP status and a machine-readable error code.
 */
@Getter
public class MoneyPayException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public MoneyPayException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    public MoneyPayException(String message, HttpStatus status, String errorCode, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }
}
