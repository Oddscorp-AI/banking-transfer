package com.example.banking.logging;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Adds a requestUid to the MDC for each request so logs can be correlated.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String id = Optional.ofNullable(request.getHeader("X-Request-Id"))
                .orElse(UUID.randomUUID().toString());
        MDC.put("requestUid", id);
        try {
            response.setHeader("X-Request-Id", id);
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("requestUid");
        }
    }
}
