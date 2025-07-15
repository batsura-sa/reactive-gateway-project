package com.example.ratelimit;

import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RateLimitService
 */
@ExtendWith(MockitoExtension.class)
class RateLimitServiceTest {
    
    @Mock
    private RedisClient redisClient;
    
    private RateLimitConfig config;
    private RateLimitService rateLimitService;
    
    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        config.setEnabled(true);
        config.setUseRedis(false); // Use in-memory for tests
        config.setKeyPrefix("test_rate_limit:");
        
        // Set up endpoint configs
        RateLimitConfig.EndpointConfig getUserByIdConfig = new RateLimitConfig.EndpointConfig();
        getUserByIdConfig.setCapacity(5);
        getUserByIdConfig.setRefillTokens(5);
        getUserByIdConfig.setRefillPeriod(java.time.Duration.ofMinutes(1));
        config.setGetUserById(getUserByIdConfig);
        
        rateLimitService = new RateLimitService(config, redisClient);
    }
    
    @Test
    void testRateLimitDisabled() {
        // Given
        config.setEnabled(false);
        rateLimitService = new RateLimitService(config, redisClient);
        
        // When & Then
        StepVerifier.create(rateLimitService.isAllowed("test-key", "getUserById"))
                .assertNext(result -> {
                    assertTrue(result.isAllowed());
                    assertEquals(-1, result.getRemainingTokens());
                })
                .verifyComplete();
    }
    
    @Test
    void testInMemoryRateLimitAllowed() {
        // When & Then
        StepVerifier.create(rateLimitService.isAllowed("test-key", "getUserById"))
                .assertNext(result -> {
                    assertTrue(result.isAllowed());
                    assertTrue(result.getRemainingTokens() >= 0);
                })
                .verifyComplete();
    }
    
    @Test
    void testInMemoryRateLimitExceeded() {
        // Given - consume all tokens
        String key = "test-key-exceeded";
        String endpoint = "getUserById";
        
        // When - make requests up to the limit
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(rateLimitService.isAllowed(key, endpoint))
                    .assertNext(result -> assertTrue(result.isAllowed()))
                    .verifyComplete();
        }
        
        // Then - next request should be rate limited
        StepVerifier.create(rateLimitService.isAllowed(key, endpoint))
                .assertNext(result -> {
                    assertFalse(result.isAllowed());
                    assertEquals(0, result.getRemainingTokens());
                    assertTrue(result.getRetryAfter().toMillis() > 0);
                })
                .verifyComplete();
    }
    
    @Test
    void testDifferentEndpointsSeparateRateLimits() {
        // Given
        String key = "test-key-separate";
        
        // When - consume all tokens for getUserById
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(rateLimitService.isAllowed(key, "getUserById"))
                    .assertNext(result -> assertTrue(result.isAllowed()))
                    .verifyComplete();
        }
        
        // Then - getUserById should be rate limited
        StepVerifier.create(rateLimitService.isAllowed(key, "getUserById"))
                .assertNext(result -> assertFalse(result.isAllowed()))
                .verifyComplete();
        
        // But createUser should still work (different endpoint)
        StepVerifier.create(rateLimitService.isAllowed(key, "createUser"))
                .assertNext(result -> assertTrue(result.isAllowed()))
                .verifyComplete();
    }
    
    @Test
    void testDifferentKeysSeparateRateLimits() {
        // Given
        String key1 = "test-key-1";
        String key2 = "test-key-2";
        String endpoint = "getUserById";
        
        // When - consume all tokens for key1
        for (int i = 0; i < 5; i++) {
            StepVerifier.create(rateLimitService.isAllowed(key1, endpoint))
                    .assertNext(result -> assertTrue(result.isAllowed()))
                    .verifyComplete();
        }
        
        // Then - key1 should be rate limited
        StepVerifier.create(rateLimitService.isAllowed(key1, endpoint))
                .assertNext(result -> assertFalse(result.isAllowed()))
                .verifyComplete();
        
        // But key2 should still work
        StepVerifier.create(rateLimitService.isAllowed(key2, endpoint))
                .assertNext(result -> assertTrue(result.isAllowed()))
                .verifyComplete();
    }
}