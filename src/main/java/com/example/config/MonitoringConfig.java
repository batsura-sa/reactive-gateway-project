package com.example.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import java.time.Duration;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Configuration for monitoring and metrics
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {
    
    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    /**
     * Customize meter registry with common tags and filters
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> {
            // Add common tags to all metrics
            registry.config()
                    .commonTags("application", "reactive-gateway")
                    .commonTags("version", "1.0.0")
                    .meterFilter(MeterFilter.deny(id -> {
                        // Filter out noisy metrics
                        String name = id.getName();
                        return name.startsWith("jvm.gc.pause") ||
                               name.startsWith("process.files") ||
                               name.startsWith("system.load.average");
                    }))
                    .meterFilter(MeterFilter.denyNameStartsWith("jvm.gc.pause"));
        };
    }
}