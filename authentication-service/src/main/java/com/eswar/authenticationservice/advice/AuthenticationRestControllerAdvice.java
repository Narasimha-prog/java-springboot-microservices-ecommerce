package com.eswar.authenticationservice.advice;

import com.eswar.authenticationservice.exception.UserNotFoundException;
import com.eswar.authenticationservice.exception.UserServiceUnavailableException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class AuthenticationRestControllerAdvice {

    // Handle UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException ex,
                                            HttpServletRequest request) {

        log.error("UserNotFoundException: {}", ex.getMessage());

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problemDetail.setTitle("User Not Found");
        problemDetail.setDetail(ex.getMessage());

        // custom properties
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }


    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneralException(Exception ex,
                                                HttpServletRequest request) {

        log.error("Unhandled exception: ", ex);

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(ex.getMessage());

        // custom properties
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ProblemDetail handleServiceUnavailable(
            UserServiceUnavailableException ex,
            HttpServletRequest request) {

        log.error(" UserServiceUnavailableException: {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problem.setTitle("User Service Unavailable");
        problem.setDetail(ex.getMessage());
        problem.setProperty("path", request.getRequestURI());

        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.error("Validation failed: {}", ex.getMessage());

        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail("One or more fields are invalid");

        // collect field errors
        Map<String, String> errors = new HashMap<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        return problemDetail;
    }
}