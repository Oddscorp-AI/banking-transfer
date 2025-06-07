package com.example.banking.security;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory rate limiting filter. Limits each remote address to
 * 100 requests per minute.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitFilter extends OncePerRequestFilter {

    private static class Counter {
        AtomicInteger count = new AtomicInteger();
        Instant windowStart = Instant.now();
    }

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Duration window = Duration.ofMinutes(1);
    private final int limit = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String key = request.getRemoteAddr();
        Counter counter = counters.computeIfAbsent(key, k -> new Counter());
        synchronized (counter) {
            if (Duration.between(counter.windowStart, Instant.now()).compareTo(window) > 0) {
                counter.count.set(0);
                counter.windowStart = Instant.now();
            }
            if (counter.count.incrementAndGet() > limit) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
