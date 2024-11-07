package org.crypto.recommendations.crypto_recommendation_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    private RateLimitFilter rateLimitFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = Mockito.mock(FilterChain.class);
    }

    @Test
    void shouldAllowRequestsUnderLimit() throws ServletException, IOException {
        // Simulate multiple requests within limit
        for (int i = 0; i < 10; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }
    }

    @Test
    void shouldReturnTooManyRequestsWhenLimitExceeded() throws ServletException, IOException {
        // Exceed the rate limit (simulate 101 requests)
        for (int i = 0; i < 101; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
        }

        // Verify the response status and body when limit is exceeded
        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("Rate limit exceeded"));
    }

    @Test
    void shouldApplyRateLimitPerIpAddress() throws ServletException, IOException {
        // Set IP for first request
        request.setRemoteAddr("192.168.1.1");

        // Simulate 100 requests from IP 192.168.1.1 (should be within limit)
        for (int i = 0; i < 100; i++) {
            rateLimitFilter.doFilter(request, response, filterChain);
            assertEquals(200, response.getStatus());
        }

        // Exceed the limit with one more request from IP 192.168.1.1
        rateLimitFilter.doFilter(request, response, filterChain);
        assertEquals(429, response.getStatus());

        // Reset response for the next IP test
        response = new MockHttpServletResponse();

        // Set a different IP for the next request
        request.setRemoteAddr("192.168.1.2");

        // The new IP should have a fresh rate limit, so it should be allowed
        rateLimitFilter.doFilter(request, response, filterChain);
        assertEquals(200, response.getStatus());
    }
}
