package com.example.config;

import com.example.metrics.CustomMetrics;
import com.example.ratelimit.RateLimitConfig;
import com.example.ratelimit.RateLimitKeyResolver;
import com.example.ratelimit.RateLimitService;
import io.lettuce.core.RedisClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration to provide mocked beans for testing
 */
@TestConfiguration
public class TestConfig {
    
    @Bean
    @Primary
    public MeterRegistry testMeterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @Bean
    @Primary
    public CustomMetrics testCustomMetrics() {
        return new CustomMetrics(testMeterRegistry());
    }
    
    @Bean
    @Primary
    public RateLimitConfig testRateLimitConfig() {
        RateLimitConfig config = new RateLimitConfig();
        config.setEnabled(false); // Disable rate limiting in tests
        return config;
    }
    
    @Bean
    @Primary
    public RedisClient testRedisClient() {
        return Mockito.mock(RedisClient.class);
    }
    
    @Bean
    @Primary
    public RateLimitService testRateLimitService() {
        RateLimitService mockService = Mockito.mock(RateLimitService.class);
        // Configure mock to return allowed by default
        RateLimitService.RateLimitResult allowedResult = 
            new RateLimitService.RateLimitResult(true, 100, 100, java.time.Duration.ZERO);
        Mockito.when(mockService.isAllowed(Mockito.anyString(), Mockito.anyString()))
               .thenReturn(reactor.core.publisher.Mono.just(allowedResult));
        return mockService;
    }
    
    @Bean
    @Primary
    public RateLimitKeyResolver testRateLimitKeyResolver() {
        return Mockito.mock(RateLimitKeyResolver.class);
    }
}