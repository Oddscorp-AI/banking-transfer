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
        MockHttpServletRequest req = new MockHttpServletRequest();

        // First 100 requests should pass
        for (int i = 0; i < 100; i++) {
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();
            filter.doFilter(req, res, chain);
            assertNotEquals(429, res.getStatus(),
                    "Request #" + (i+1) + " should not be rate-limited");
        }

        // 101st request should be blocked
        MockHttpServletResponse res101 = new MockHttpServletResponse();
        MockFilterChain chain101 = new MockFilterChain();
        filter.doFilter(req, res101, chain101);
        assertEquals(429, res101.getStatus(),
                "101st request should be rate-limited");
    }
}
