package com.example.ratelimit;

import com.example.metrics.CustomMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * WebFilter for rate limiting HTTP requests
 */
@Component
@Order(-100) // High priority to run early in the filter chain
public class RateLimitFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    
    private final RateLimitService rateLimitService;
    private final RateLimitKeyResolver keyResolver;
    private final CustomMetrics customMetrics;
    
    public RateLimitFilter(RateLimitService rateLimitService, 
                          RateLimitKeyResolver keyResolver,
                          CustomMetrics customMetrics) {
        this.rateLimitService = rateLimitService;
        this.keyResolver = keyResolver;
        this.customMetrics = customMetrics;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        
        // Skip rate limiting for actuator endpoints
        if (path.startsWith("/actuator")) {
            return chain.filter(exchange);
        }
        
        String key = keyResolver.resolve(exchange);
        String endpoint = extractEndpoint(path, request.getMethod().name());
        
        return rateLimitService.isAllowed(key, endpoint)
            .flatMap(result -> {
                if (result.isAllowed()) {
                    // Add rate limit headers
                    addRateLimitHeaders(exchange.getResponse(), result);
                    return chain.filter(exchange);
                } else {
                    // Rate limit exceeded
                    logger.warn("Rate limit exceeded for key: {}, endpoint: {}", key, endpoint);
                    customMetrics.incrementRateLimitExceeded();
                    return handleRateLimitExceeded(exchange, result);
                }
            })
            .onErrorResume(error -> {
                logger.error("Error in rate limit filter", error);
                // Fail open - continue with request if rate limiting fails
                return chain.filter(exchange);
            });
    }
    
    private void addRateLimitHeaders(ServerHttpResponse response, RateLimitService.RateLimitResult result) {
        if (result.getRemainingTokens() >= 0) {
            response.getHeaders().add("X-RateLimit-Remaining", String.valueOf(result.getRemainingTokens()));
        }
        if (result.getAvailableTokens() >= 0) {
            response.getHeaders().add("X-RateLimit-Available", String.valueOf(result.getAvailableTokens()));
        }
        if (!result.getRetryAfter().isZero()) {
            response.getHeaders().add("X-RateLimit-Retry-After", String.valueOf(result.getRetryAfter().getSeconds()));
        }
    }
    
    private Mono<Void> handleRateLimitExceeded(ServerWebExchange exchange, RateLimitService.RateLimitResult result) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        
        // Add rate limit headers
        addRateLimitHeaders(response, result);
        
        if (!result.getRetryAfter().isZero()) {
            response.getHeaders().add("Retry-After", String.valueOf(result.getRetryAfter().getSeconds()));
        }
        
        // Return JSON error response
        String errorBody = """
            {
                "error": "Rate limit exceeded",
                "message": "Too many requests. Please try again later.",
                "status": 429,
                "timestamp": "%s"
            }
            """.formatted(java.time.Instant.now().toString());
        
        response.getHeaders().add("Content-Type", "application/json");
        var buffer = response.bufferFactory().wrap(errorBody.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
    
    private String extractEndpoint(String path, String method) {
        // Extract endpoint name from path for rate limiting
        if (path.startsWith("/api/users")) {
            if (path.equals("/api/users") && "GET".equals(method)) {
                return "listUsers";
            } else if (path.equals("/api/users") && "POST".equals(method)) {
                return "createUser";
            } else if (path.matches("/api/users/[^/]+") && "GET".equals(method)) {
                return "getUserById";
            } else if (path.matches("/api/users/[^/]+") && "PUT".equals(method)) {
                return "updateUser";
            } else if (path.matches("/api/users/[^/]+") && "DELETE".equals(method)) {
                return "deleteUser";
            }
        }
        
        // Default endpoint name
        return path.replaceAll("/", "_") + "_" + method.toLowerCase();
    }
}