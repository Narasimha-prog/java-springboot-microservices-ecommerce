package com.eswar.swagger_service.advice;

import com.eswar.swagger_service.exception.BusinessException;
import com.eswar.swagger_service.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class SwaggerRestControllerAdvice {

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex) {
        log.warn(" exception occurred from BusinessException", ex);
        ProblemDetail problem = ProblemDetail.forStatus(ex.getStatus());

        problem.setTitle("Business Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", ex.getErrorCode());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
    // ------------------ VALIDATION ERRORS ------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn(" exception occurred from handleValidationErrors", ex);
        ProblemDetail pd = ProblemDetail.forStatus(ErrorCode.VALIDATION_FAILED.getStatus());
        pd.setTitle("Validation Failed");
        pd.setDetail("One or more fields have errors");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("errorCode", ErrorCode.VALIDATION_FAILED.name());

        Map<String, Map<String, String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> Map.of(
                                "errorCode", mapFieldToErrorCode(fieldError.getField()),
                                "message", fieldError.getDefaultMessage()
                        )
                ));
        pd.setProperty("errors", errors);

        return pd;
    }
    private String mapFieldToErrorCode(String field) {
        return switch (field) {
            case "email" -> ErrorCode.INVALID_REQUEST.name();
            case "password" -> ErrorCode.INVALID_REQUEST.name();
            case "phoneNumber" -> ErrorCode.USER_ALREADY_EXISTS.name();
            case "firstName", "lastName" -> ErrorCode.INVALID_REQUEST.name();
            default -> ErrorCode.VALIDATION_FAILED.name();
        };
    }

    // ------------------ HTTP EXCEPTIONS ------------------
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildProblemDetail(ErrorCode.METHOD_NOT_ALLOWED, "HTTP method " + ex.getMethod() + " not allowed.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        return buildProblemDetail(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Content-Type " + ex.getContentType() + " is not supported.");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ProblemDetail handleNotAcceptable(HttpMediaTypeNotAcceptableException ex) {
        return buildProblemDetail(ErrorCode.NOT_ACCEPTABLE, "Media type not acceptable.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleMalformedJson(HttpMessageNotReadableException ex) {
        return buildProblemDetail(ErrorCode.MALFORMED_REQUEST, "Request body is malformed.");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ProblemDetail handleMissingParams(MissingServletRequestParameterException ex) {
        return buildProblemDetail(ErrorCode.MISSING_PARAMETER, "Required parameter '" + ex.getParameterName() + "' is missing.");
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(TypeMismatchException ex) {
        return buildProblemDetail(ErrorCode.TYPE_MISMATCH, "Parameter '" + ex.getPropertyName() + "' has invalid type.");
    }
    // ------------------ UTILITY ------------------
    private ProblemDetail buildProblemDetail(ErrorCode errorCode, String detail) {
        ProblemDetail pd = ProblemDetail.forStatus(errorCode.getStatus());
        pd.setTitle(errorCode.getMessage());
        pd.setDetail(detail);
        pd.setProperty("errorCode", errorCode.name());
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatus(
                ErrorCode.INTERNAL_ERROR.getStatus()
        );

        problem.setTitle("Internal Server Error");
        problem.setDetail(ex.getMessage());
        problem.setProperty("errorCode", ErrorCode.INTERNAL_ERROR.name());
        problem.setProperty("timestamp", Instant.now());

        return problem;
    }
}
