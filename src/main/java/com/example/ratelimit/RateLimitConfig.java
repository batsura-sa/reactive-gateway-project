package com.example.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration properties for rate limiting
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {
    
    private boolean enabled = true;
    private Strategy strategy = Strategy.TOKEN_BUCKET;
    private Duration window = Duration.ofMinutes(1);
    private int capacity = 100;
    private int refillTokens = 100;
    private Duration refillPeriod = Duration.ofMinutes(1);
    private boolean useRedis = false;
    private String keyPrefix = "rate_limit:";
    
    // Per-endpoint configurations
    private EndpointConfig getUserById = new EndpointConfig(60, 60, Duration.ofMinutes(1));
    private EndpointConfig createUser = new EndpointConfig(10, 10, Duration.ofMinutes(1));
    private EndpointConfig updateUser = new EndpointConfig(20, 20, Duration.ofMinutes(1));
    private EndpointConfig deleteUser = new EndpointConfig(5, 5, Duration.ofMinutes(1));
    private EndpointConfig listUsers = new EndpointConfig(30, 30, Duration.ofMinutes(1));
    
    public enum Strategy {
        TOKEN_BUCKET,
        FIXED_WINDOW,
        SLIDING_WINDOW
    }
    
    public static class EndpointConfig {
        private int capacity;
        private int refillTokens;
        private Duration refillPeriod;
        
        public EndpointConfig() {}
        
        public EndpointConfig(int capacity, int refillTokens, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriod = refillPeriod;
        }
        
        // Getters and setters
        public int getCapacity() { return capacity; }
        public void setCapacity(int capacity) { this.capacity = capacity; }
        
        public int getRefillTokens() { return refillTokens; }
        public void setRefillTokens(int refillTokens) { this.refillTokens = refillTokens; }
        
        public Duration getRefillPeriod() { return refillPeriod; }
        public void setRefillPeriod(Duration refillPeriod) { this.refillPeriod = refillPeriod; }
    }
    
    // Getters and setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    public Strategy getStrategy() { return strategy; }
    public void setStrategy(Strategy strategy) { this.strategy = strategy; }
    
    public Duration getWindow() { return window; }
    public void setWindow(Duration window) { this.window = window; }
    
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    
    public int getRefillTokens() { return refillTokens; }
    public void setRefillTokens(int refillTokens) { this.refillTokens = refillTokens; }
    
    public Duration getRefillPeriod() { return refillPeriod; }
    public void setRefillPeriod(Duration refillPeriod) { this.refillPeriod = refillPeriod; }
    
    public boolean isUseRedis() { return useRedis; }
    public void setUseRedis(boolean useRedis) { this.useRedis = useRedis; }
    
    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }
    
    public EndpointConfig getGetUserById() { return getUserById; }
    public void setGetUserById(EndpointConfig getUserById) { this.getUserById = getUserById; }
    
    public EndpointConfig getCreateUser() { return createUser; }
    public void setCreateUser(EndpointConfig createUser) { this.createUser = createUser; }
    
    public EndpointConfig getUpdateUser() { return updateUser; }
    public void setUpdateUser(EndpointConfig updateUser) { this.updateUser = updateUser; }
    
    public EndpointConfig getDeleteUser() { return deleteUser; }
    public void setDeleteUser(EndpointConfig deleteUser) { this.deleteUser = deleteUser; }
    
    public EndpointConfig getListUsers() { return listUsers; }
    public void setListUsers(EndpointConfig listUsers) { this.listUsers = listUsers; }
}