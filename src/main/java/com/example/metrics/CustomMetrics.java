package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom metrics for the reactive gateway
 */
@Component
public class CustomMetrics {
    
    private final Counter userCreatedCounter;
    private final Counter userDeletedCounter;
    private final Counter grpcErrorCounter;
    private final Counter rateLimitExceededCounter;
    private final Timer grpcRequestTimer;
    private final Timer httpRequestTimer;
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    
    public CustomMetrics(MeterRegistry meterRegistry) {
        // Counters
        this.userCreatedCounter = Counter.builder("users.created.total")
                .description("Total number of users created")
                .register(meterRegistry);
        
        this.userDeletedCounter = Counter.builder("users.deleted.total")
                .description("Total number of users deleted")
                .register(meterRegistry);
        
        this.grpcErrorCounter = Counter.builder("grpc.errors.total")
                .description("Total number of gRPC errors")
                .tag("service", "user-service")
                .register(meterRegistry);
        
        this.rateLimitExceededCounter = Counter.builder("rate.limit.exceeded.total")
                .description("Total number of rate limit exceeded events")
                .register(meterRegistry);
        
        // Timers
        this.grpcRequestTimer = Timer.builder("grpc.request.duration")
                .description("gRPC request duration")
                .tag("service", "user-service")
                .register(meterRegistry);
        
        this.httpRequestTimer = Timer.builder("http.gateway.request.duration")
                .description("HTTP gateway request duration")
                .register(meterRegistry);
        
        // Gauges
        Gauge.builder("gateway.active.connections", this, metrics -> metrics.activeConnections.doubleValue())
                .description("Number of active connections")
                .register(meterRegistry);
    }
    
    public void incrementUserCreated() {
        userCreatedCounter.increment();
    }
    
    public void incrementUserDeleted() {
        userDeletedCounter.increment();
    }
    
    public void incrementGrpcError() {
        grpcErrorCounter.increment();
    }
    
    public Timer.Sample startGrpcTimer() {
        return Timer.start();
    }
    
    public void stopGrpcTimer(Timer.Sample sample) {
        sample.stop(grpcRequestTimer);
    }
    
    public Timer.Sample startHttpTimer() {
        return Timer.start();
    }
    
    public void stopHttpTimer(Timer.Sample sample) {
        sample.stop(httpRequestTimer);
    }
    
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }
    
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
    
    public void incrementRateLimitExceeded() {
        rateLimitExceededCounter.increment();
    }
}