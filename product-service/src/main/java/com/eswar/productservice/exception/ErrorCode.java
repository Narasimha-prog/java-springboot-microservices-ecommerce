package com.eswar.productservice.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
   //user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already registered"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Phone number already registered"),

    //auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    //bus
    PRODUCT_NOT_FOUND( HttpStatus.NOT_FOUND, "Product not found: "),
    CATEGORY_NOT_FOUND( HttpStatus.NOT_FOUND, "Category not found: "),
    CATEGORY_ALREADY_EXISTS( HttpStatus.NOT_FOUND, "Category already exists: "),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Product already exists: "),

    // service (VERY IMPORTANT for microservices)
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
    DOWNSTREAM_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Error from downstream service"),
    TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Request timeout"),
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
