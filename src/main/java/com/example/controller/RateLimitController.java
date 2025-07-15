package com.example.controller;

import com.example.ratelimit.RateLimitConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Controller for rate limit management and monitoring
 */
@RestController
@RequestMapping("/api/rate-limit")
public class RateLimitController {
    
    private final RateLimitConfig rateLimitConfig;
    
    public RateLimitController(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }
    
    /**
     * Get current rate limit configuration
     */
    @GetMapping("/config")
    public Mono<Map<String, Object>> getRateLimitConfig() {
        return Mono.just(Map.of(
            "enabled", rateLimitConfig.isEnabled(),
            "strategy", rateLimitConfig.getStrategy(),
            "useRedis", rateLimitConfig.isUseRedis(),
            "defaultCapacity", rateLimitConfig.getCapacity(),
            "defaultRefillTokens", rateLimitConfig.getRefillTokens(),
            "defaultRefillPeriod", rateLimitConfig.getRefillPeriod().toString(),
            "endpoints", Map.of(
                "getUserById", Map.of(
                    "capacity", rateLimitConfig.getGetUserById().getCapacity(),
                    "refillTokens", rateLimitConfig.getGetUserById().getRefillTokens(),
                    "refillPeriod", rateLimitConfig.getGetUserById().getRefillPeriod().toString()
                ),
                "createUser", Map.of(
                    "capacity", rateLimitConfig.getCreateUser().getCapacity(),
                    "refillTokens", rateLimitConfig.getCreateUser().getRefillTokens(),
                    "refillPeriod", rateLimitConfig.getCreateUser().getRefillPeriod().toString()
                ),
                "updateUser", Map.of(
                    "capacity", rateLimitConfig.getUpdateUser().getCapacity(),
                    "refillTokens", rateLimitConfig.getUpdateUser().getRefillTokens(),
                    "refillPeriod", rateLimitConfig.getUpdateUser().getRefillPeriod().toString()
                ),
                "deleteUser", Map.of(
                    "capacity", rateLimitConfig.getDeleteUser().getCapacity(),
                    "refillTokens", rateLimitConfig.getDeleteUser().getRefillTokens(),
                    "refillPeriod", rateLimitConfig.getDeleteUser().getRefillPeriod().toString()
                ),
                "listUsers", Map.of(
                    "capacity", rateLimitConfig.getListUsers().getCapacity(),
                    "refillTokens", rateLimitConfig.getListUsers().getRefillTokens(),
                    "refillPeriod", rateLimitConfig.getListUsers().getRefillPeriod().toString()
                )
            )
        ));
    }
    
    /**
     * Health check for rate limiting service
     */
    @GetMapping("/health")
    public Mono<Map<String, Object>> getRateLimitHealth() {
        return Mono.just(Map.of(
            "status", "UP",
            "rateLimitEnabled", rateLimitConfig.isEnabled(),
            "backend", rateLimitConfig.isUseRedis() ? "Redis" : "In-Memory"
        ));
    }
}