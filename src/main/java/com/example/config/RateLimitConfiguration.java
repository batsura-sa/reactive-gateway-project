package com.example.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import com.example.ratelimit.RateLimitConfig;
import java.time.Duration;

/**
 * Configuration for rate limiting infrastructure
 */
@Configuration
//@EnableConfigurationProperties(RateLimitConfig.class)
public class RateLimitConfiguration {
    
    /**
     * Redis client for distributed rate limiting
     */
    @Bean
    @ConditionalOnProperty(name = "rate-limit.use-redis", havingValue = "true")
    public RedisClient redisClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port,
            @Value("${spring.data.redis.password:}") String password,
            @Value("${spring.data.redis.database:0}") int database,
            @Value("${spring.data.redis.ssl:false}") boolean ssl,
            @Value("${spring.data.redis.timeout:2000ms}") Duration timeout) {
        
        RedisURI.Builder builder = RedisURI.Builder
            .redis(host, port)
            .withDatabase(database)
            .withTimeout(timeout);
        
        if (password != null && !password.isEmpty()) {
            builder.withPassword(password.toCharArray());
        }
        
        if (ssl) {
            builder.withSsl(true);
        }
        
        RedisURI redisUri = builder.build();
        
        return RedisClient.create(redisUri);
    }
    
    /**
     * Fallback Redis client when Redis is not configured for rate limiting
     */
    @Bean
    @ConditionalOnProperty(name = "rate-limit.use-redis", havingValue = "false", matchIfMissing = true)
    public RedisClient fallbackRedisClient() {
        // Return a dummy client that won't be used
        return RedisClient.create("redis://localhost:6379");
    }
}