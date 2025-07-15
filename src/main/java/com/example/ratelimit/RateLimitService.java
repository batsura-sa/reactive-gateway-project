package com.example.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Service for rate limiting using Bucket4j
 */
@Service
public class RateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitService.class);
    
    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, Bucket> localBuckets = new ConcurrentHashMap<>();
    private final ProxyManager<String> proxyManager;
    
    public RateLimitService(RateLimitConfig config, RedisClient redisClient) {
        this.config = config;
        
        if (config.isUseRedis()) {
            StatefulRedisConnection<String, byte[]> redisConnection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
            );
            this.proxyManager = LettuceBasedProxyManager.builderFor(redisConnection)
                .build();
            logger.info("Rate limiting initialized with Redis backend");
        } else {
            this.proxyManager = null;
            logger.info("Rate limiting initialized with in-memory backend");
        }
    }
    
    /**
     * Check if request is allowed for the given key and endpoint
     */
    public Mono<RateLimitResult> isAllowed(String key, String endpoint) {
        if (!config.isEnabled()) {
            return Mono.just(new RateLimitResult(true, -1, -1, Duration.ZERO));
        }
        
        try {
            if (config.isUseRedis() && proxyManager != null) {
                return checkRateLimitDistributed(key, endpoint);
            } else {
                return checkRateLimitLocal(key, endpoint);
            }
        } catch (Exception e) {
            logger.error("Error checking rate limit for key: {}, endpoint: {}", key, endpoint, e);
            // Fail open - allow request if rate limiting fails
            return Mono.just(new RateLimitResult(true, -1, -1, Duration.ZERO));
        }
    }
    
    private Mono<RateLimitResult> checkRateLimitLocal(String key, String endpoint) {
        String bucketKey = config.getKeyPrefix() + endpoint + ":" + key;
        
        Bucket bucket = localBuckets.computeIfAbsent(bucketKey, k -> {
            RateLimitConfig.EndpointConfig endpointConfig = getEndpointConfig(endpoint);
            Bandwidth bandwidth = Bandwidth.builder()
                .capacity(endpointConfig.getCapacity())
                .refillIntervally(endpointConfig.getRefillTokens(), endpointConfig.getRefillPeriod())
                .build();
            return Bucket.builder()
                .addLimit(bandwidth)
                .build();
        });
        
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            return Mono.just(new RateLimitResult(
                true, 
                probe.getRemainingTokens(), 
                bucket.getAvailableTokens(),
                Duration.ZERO
            ));
        } else {
            return Mono.just(new RateLimitResult(
                false, 
                0, 
                bucket.getAvailableTokens(),
                probe.getNanosToWaitForRefill() > 0 ? 
                    Duration.ofNanos(probe.getNanosToWaitForRefill()) : Duration.ZERO
            ));
        }
    }
    
    private Mono<RateLimitResult> checkRateLimitDistributed(String key, String endpoint) {
        String bucketKey = config.getKeyPrefix() + endpoint + ":" + key;
        
        Supplier<BucketConfiguration> configSupplier = () -> createBucketConfiguration(endpoint);
        var bucket = proxyManager.builder().build(bucketKey, configSupplier);
        
        return Mono.fromCallable(() -> bucket.tryConsumeAndReturnRemaining(1))
            .map(probe -> {
                if (probe.isConsumed()) {
                    return new RateLimitResult(
                        true, 
                        probe.getRemainingTokens(), 
                        -1, // Not available in distributed mode
                        Duration.ZERO
                    );
                } else {
                    return new RateLimitResult(
                        false, 
                        0, 
                        -1,
                        probe.getNanosToWaitForRefill() > 0 ? 
                            Duration.ofNanos(probe.getNanosToWaitForRefill()) : Duration.ZERO
                    );
                }
            })
            .onErrorReturn(new RateLimitResult(true, -1, -1, Duration.ZERO)); // Fail open
    }
    
    private BucketConfiguration createBucketConfiguration(String endpoint) {
        RateLimitConfig.EndpointConfig endpointConfig = getEndpointConfig(endpoint);
        
        Bandwidth bandwidth = Bandwidth.builder()
            .capacity(endpointConfig.getCapacity())
            .refillIntervally(endpointConfig.getRefillTokens(), endpointConfig.getRefillPeriod())
            .build();
        
        return BucketConfiguration.builder()
            .addLimit(bandwidth)
            .build();
    }
    
    private RateLimitConfig.EndpointConfig getEndpointConfig(String endpoint) {
        return switch (endpoint.toLowerCase()) {
            case "getuserbyid" -> config.getGetUserById();
            case "createuser" -> config.getCreateUser();
            case "updateuser" -> config.getUpdateUser();
            case "deleteuser" -> config.getDeleteUser();
            case "listusers" -> config.getListUsers();
            default -> new RateLimitConfig.EndpointConfig(
                config.getCapacity(), 
                config.getRefillTokens(), 
                config.getRefillPeriod()
            );
        };
    }
    
    /**
     * Result of rate limit check
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingTokens;
        private final long availableTokens;
        private final Duration retryAfter;
        
        public RateLimitResult(boolean allowed, long remainingTokens, long availableTokens, Duration retryAfter) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
            this.availableTokens = availableTokens;
            this.retryAfter = retryAfter;
        }
        
        public boolean isAllowed() { return allowed; }
        public long getRemainingTokens() { return remainingTokens; }
        public long getAvailableTokens() { return availableTokens; }
        public Duration getRetryAfter() { return retryAfter; }
    }
}