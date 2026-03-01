package com.eswar.authenticationservice.advice;

import com.eswar.authenticationservice.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

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
}