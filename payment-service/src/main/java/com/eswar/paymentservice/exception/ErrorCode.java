package com.eswar.paymentservice.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // validation
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Request body is malformed"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "Required parameter is missing"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Parameter type mismatch"),
    //auth
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    // HTTP method/content errors
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not acceptable media type"),
 // 🔐 Generic
 INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),

 // 💳 Payment errors
 PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
 PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied to this payment"),
 PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "Payment already completed"),
 PAYMENT_ALREADY_VERIFIED(HttpStatus.CONFLICT, "Payment already verified"),

 // 💰 Razorpay
 INVALID_PAYMENT_SIGNATURE(HttpStatus.BAD_REQUEST, "Invalid payment signature"),
 INVALID_WEBHOOK_SIGNATURE(HttpStatus.UNAUTHORIZED, "Invalid webhook signature"),
    INVALID_WEBHOOK_PAYLOAD(HttpStatus.BAD_REQUEST,"Invalid webhook payload"),
 // 🔧 External service
 PAYMENT_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Payment provider unavailable");


 private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
