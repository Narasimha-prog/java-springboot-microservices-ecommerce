package com.eswar.inventoryservice.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    //auth
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token expired"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
    TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "Malformed token"),
    ACCESS_DENIED(HttpStatus.BAD_REQUEST,"Access is not allowed"),

    // service (VERY IMPORTANT for microservices)
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
    DOWNSTREAM_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Error from downstream service"),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Request timeout"),
    PRODUCT_NOT_FOUND( HttpStatus.NOT_FOUND, "Product not found: "),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST,"Insufficient Stock"),

    // validation
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST, "Request body is malformed"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "Required parameter is missing"),
    TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "Parameter type mismatch"),

    // HTTP method/content errors
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported media type"),
    NOT_ACCEPTABLE(HttpStatus.NOT_ACCEPTABLE, "Not acceptable media type"),

    //genric
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");

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
