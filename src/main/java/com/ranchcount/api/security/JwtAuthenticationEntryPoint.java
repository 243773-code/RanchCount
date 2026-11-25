package com.ranchcount.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ranchcount.api.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        Object customJwtError = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTRIBUTE);
        String message = customJwtError != null
                ? customJwtError.toString()
                : "Autenticacion requerida: proporcione un token Bearer JWT valido";

        ApiErrorResponse errorResponse = ApiErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                message,
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
