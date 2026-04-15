package com.lnreddy.cart_service.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

 // ================= USER =================
 USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
 USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "User already exists"),
 INVALID_USER_ID(HttpStatus.BAD_REQUEST, "Invalid user ID"),
 ACCESS_DENIED(HttpStatus.BAD_REQUEST,"Access is not allowed"),

 // ================= ORDER =================
 ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
 ORDER_INVALID_ID(HttpStatus.BAD_REQUEST, "Invalid order ID"),
 ORDER_CANNOT_UPDATE(HttpStatus.BAD_REQUEST, "Order cannot be updated"),
 ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "Order already cancelled"),
 ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have access to this order"),

 // ================= PRODUCT / DOWNSTREAM =================
 CART_NOT_FOUND(HttpStatus.NOT_FOUND,"Cart not found "),
 PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
 PRODUCT_SERVICE_FAILED(HttpStatus.BAD_GATEWAY, "Product service unavailable"),

 // ================= AUTH =================
 TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token expired"),
 INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
 INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
 TOKEN_MALFORMED(HttpStatus.UNAUTHORIZED, "Malformed token"),

 // ================= SERVICE =================
 SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
 DOWNSTREAM_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "Error from downstream service"),
 TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "Request timeout"),
 INSUFFICIENT_STOCK(HttpStatus.NOT_FOUND,"Stock is not there"),

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

 // ================= GENERIC =================
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