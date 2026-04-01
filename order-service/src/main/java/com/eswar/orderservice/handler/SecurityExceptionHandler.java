package com.eswar.orderservice.handler;


import com.eswar.userservice.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public SecurityExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        writeResponse(response, ErrorCode.INVALID_CREDENTIALS, authException.getMessage());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        writeResponse(response, ErrorCode.ACCESS_DENIED,
                accessDeniedException.getMessage() != null
                        ? accessDeniedException.getMessage()
                        : "You do not have permission for this resource.");
    }

    private void writeResponse(HttpServletResponse response, ErrorCode errorCode, String detail) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatus(errorCode.getStatus());
        pd.setTitle(errorCode.getMessage());
        pd.setDetail(detail);
        pd.setProperty("errorCode", errorCode.name());
        pd.setProperty("timestamp", Instant.now());

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(pd));
    }
}

