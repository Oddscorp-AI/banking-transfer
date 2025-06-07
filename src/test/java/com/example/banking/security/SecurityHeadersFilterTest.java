package com.example.banking.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class SecurityHeadersFilterTest {

    @Test
    void addsSecurityHeaders() throws Exception {
        SecurityHeadersFilter filter = new SecurityHeadersFilter();
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(req, res, chain);

        assertEquals("nosniff", res.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", res.getHeader("X-Frame-Options"));
        assertEquals("1; mode=block", res.getHeader("X-XSS-Protection"));
        assertEquals("default-src 'self'", res.getHeader("Content-Security-Policy"));
    }
}
