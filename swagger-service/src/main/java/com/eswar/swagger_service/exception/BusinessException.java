package com.eswar.swagger_service.exception;

import com.eswar.swagger_service.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // default message
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage); // override message if needed
        this.errorCode = errorCode;
    }
    public BusinessException(ErrorCode errorCode, Object detail) {
        super(errorCode.getMessage() + ": " + detail);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode.name();
    }

    public HttpStatus getStatus() {
        return errorCode.getStatus(); // 🔥 dynamic
    }
}
