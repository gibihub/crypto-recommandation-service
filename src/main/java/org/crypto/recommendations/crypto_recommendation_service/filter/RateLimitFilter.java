package org.crypto.recommendations.crypto_recommendation_service.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final long MAX_REQUESTS_PER_MINUTE = 100;
    private static final Duration TIME_PERIOD = Duration.ofMinutes(1);

    // Store buckets for each IP address
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // Create a new bucket with a limit of MAX_REQUESTS_PER_MINUTE requests per TIME_PERIOD
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(MAX_REQUESTS_PER_MINUTE, Refill.greedy(MAX_REQUESTS_PER_MINUTE, TIME_PERIOD));
        return Bucket4j.builder().addLimit(limit).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIp = httpRequest.getRemoteAddr();

        // Get or create a rate limit bucket for the client's IP address
        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> createNewBucket());

        // Check if the request can proceed
        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response); // Allow the request to proceed
        } else {
            // Rate limit exceeded, send a 429 Too Many Requests response
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Rate limit exceeded. Please try again later.");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}
