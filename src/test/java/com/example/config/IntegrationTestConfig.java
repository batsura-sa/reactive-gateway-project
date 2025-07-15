package com.example.config;

import com.example.grpc.ReactorUserServiceGrpc;
import com.example.grpc.GetUserRequest;
import com.example.grpc.GetUserResponse;
import com.example.grpc.User;
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
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

/**
 * Test configuration for integration tests that need real rate limiting functionality
 */
@TestConfiguration
@Profile("test")
public class IntegrationTestConfig {
    
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
        config.setEnabled(true); // Enable for integration tests
        config.setUseRedis(false); // Use in-memory for tests
        config.setKeyPrefix("test_rate_limit:");
        
        // Configure test-specific rate limits
        RateLimitConfig.EndpointConfig getUserByIdConfig = new RateLimitConfig.EndpointConfig();
        getUserByIdConfig.setCapacity(3);
        getUserByIdConfig.setRefillTokens(3);
        getUserByIdConfig.setRefillPeriod(java.time.Duration.ofMinutes(1));
        config.setGetUserById(getUserByIdConfig);
        
        return config;
    }
    
    @Bean
    @Primary
    public RedisClient testRedisClient() {
        // Return a mock Redis client for tests
        return RedisClient.create("redis://localhost:6379");
    }
    
    @Bean
    @Primary
    public RateLimitKeyResolver testRateLimitKeyResolver() {
        return new RateLimitKeyResolver();
    }
    
    @Bean
    @Primary
    public ReactorUserServiceGrpc.ReactorUserServiceStub mockUserServiceStub() {
        ReactorUserServiceGrpc.ReactorUserServiceStub mockStub = Mockito.mock(ReactorUserServiceGrpc.ReactorUserServiceStub.class);
        
        // Mock successful user response
        User mockUser = User.newBuilder()
            .setId("test-user-0")
            .setName("Test User")
            .setEmail("test@example.com")
            .setAge(30)
            .build();
        
        GetUserResponse mockResponse = GetUserResponse.newBuilder()
            .setUser(mockUser)
            .setFound(true)
            .build();
        
        // Configure mock to return successful response for any request
        Mockito.when(mockStub.getUser(Mockito.any(GetUserRequest.class)))
               .thenReturn(Mono.just(mockResponse));
        
        return mockStub;
    }
}