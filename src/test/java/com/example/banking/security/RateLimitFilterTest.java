package com.example.banking.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    @Test
    void blocksWhenLimitExceeded() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        // exceed limit of 100 quickly
        for (int i = 0; i < 100; i++) {
            filter.doFilter(req, res, chain);
            assertNotEquals(429, res.getStatus());
        }
        filter.doFilter(req, res, chain);
        assertEquals(429, res.getStatus());
    }
}
