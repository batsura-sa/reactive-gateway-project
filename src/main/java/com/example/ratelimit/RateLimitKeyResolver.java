package com.example.ratelimit;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetSocketAddress;

/**
 * Resolves the key to use for rate limiting
 */
@Component
public class RateLimitKeyResolver {
    
    /**
     * Resolve the rate limiting key from the request
     * This implementation uses IP address, but can be extended to use:
     * - User ID from JWT token
     * - API key
     * - Custom headers
     */
    public String resolve(ServerWebExchange exchange) {
        // Try to get real IP from headers (for proxy scenarios)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // Fallback to remote address
        InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }
        
        // Ultimate fallback
        return "unknown";
    }
    
    /**
     * Resolve key for authenticated users (if you have authentication)
     * This method can be used when you want to rate limit per user instead of per IP
     */
    public String resolveForUser(ServerWebExchange exchange) {
        // Example: Extract user ID from JWT token
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Parse JWT and extract user ID
            // This is a placeholder - implement based on your authentication mechanism
            return "user_" + authHeader.substring(7, Math.min(authHeader.length(), 20));
        }
        
        // Fallback to IP-based rate limiting
        return resolve(exchange);
    }
}