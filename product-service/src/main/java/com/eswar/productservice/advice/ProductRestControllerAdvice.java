package com.eswar.productservice.advice;

import com.eswar.productservice.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class ProductRestControllerAdvice {
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest request) {

        log.warn("Business exception occurred: {} - {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Business Error");
        problem.setDetail(ex.getMessage());

        // Build base URL dynamically
        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();

        // Link to static error documentation page
        problem.setType(URI.create(baseUrl + "/docs/errors/" + ex.getErrorCode() + ".html"));

        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("errorCode", ex.getErrorCode());

        return problem;
    }

    /**
     * Handles generic exceptions and returns a ProblemDetail with general guidance.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {

        log.error("Unhandled exception occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Internal Server Error");
        problem.setDetail("An unexpected error occurred. Please contact support.");

        String baseUrl = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort();

        problem.setType(URI.create(baseUrl + "/docs/errors/GENERIC_ERROR.html"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("exception", ex.getClass().getSimpleName());

        return problem;
    }
}
