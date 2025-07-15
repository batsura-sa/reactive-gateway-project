# Monitoring Setup for Reactive Spring Boot Gateway

This document describes the comprehensive monitoring setup using Micrometer, Prometheus, Grafana, and Zipkin for the reactive Spring Boot gateway.

## 🏗️ Architecture Overview

```
Application → Micrometer → Prometheus → Grafana
     ↓
  Zipkin (Tracing)
     ↓
AlertManager (Alerts)
```

## 📊 Monitoring Stack Components

### 1. **Micrometer** (Application Metrics)
- **Purpose**: Metrics collection within the Spring Boot application
- **Features**:
  - Custom metrics for business operations
  - HTTP request/response metrics
  - gRPC call metrics
  - JVM metrics (memory, GC, threads)
  - Connection pool metrics

### 2. **Prometheus** (Metrics Storage)
- **Purpose**: Time-series database for metrics storage
- **Access**: http://localhost:9090
- **Features**:
  - Scrapes metrics from application every 10 seconds
  - Stores historical data
  - Provides PromQL query language
  - Alert rule evaluation

### 3. **Grafana** (Visualization)
- **Purpose**: Metrics visualization and dashboards
- **Access**: http://localhost:3000 (admin/admin123)
- **Features**:
  - Pre-configured dashboards
  - Real-time monitoring
  - Alert visualization
  - Custom dashboard creation

### 4. **Zipkin** (Distributed Tracing)
- **Purpose**: Request tracing across services
- **Access**: http://localhost:9411
- **Features**:
  - End-to-end request tracing
  - Performance bottleneck identification
  - Service dependency mapping

### 5. **AlertManager** (Alert Management)
- **Purpose**: Alert routing and management
- **Access**: http://localhost:9093
- **Features**:
  - Alert routing to different channels
  - Alert grouping and deduplication
  - Silence management

## 🚀 Quick Start

### 1. Start Monitoring Stack
```bash
./scripts/start-monitoring.sh
```

### 2. Start Application
```bash
./gradlew bootRun --args='--spring.profiles.active=mock-grpc'
```

### 3. Generate Load for Testing
```bash
./scripts/load-test.sh
```

### 4. Access Dashboards
- **Grafana**: http://localhost:3000
- **Prometheus**: http://localhost:9090
- **Zipkin**: http://localhost:9411

## 📈 Available Metrics

### HTTP Metrics
- `http_server_requests_total` - Total HTTP requests
- `http_server_requests_duration_seconds` - Request duration
- `http_gateway_request_duration` - Custom gateway request timing

### gRPC Metrics
- `grpc_request_duration` - gRPC call duration
- `grpc_errors_total` - gRPC error count

### Business Metrics
- `users_created_total` - Total users created
- `users_deleted_total` - Total users deleted
- `gateway_active_connections` - Active connections

### JVM Metrics
- `jvm_memory_used_bytes` - JVM memory usage
- `jvm_gc_pause_seconds` - Garbage collection metrics
- `jvm_threads_live_threads` - Thread count

### System Metrics
- `system_cpu_usage` - CPU usage
- `system_load_average_1m` - System load

## 🎯 Key Dashboards

### Reactive Gateway Dashboard
- **HTTP Request Rate**: Requests per second
- **Response Times**: P50, P95, P99 percentiles
- **gRPC Performance**: Call duration and error rates
- **Active Connections**: Real-time connection count
- **JVM Memory**: Heap usage and GC metrics
- **Custom Metrics**: Business operation metrics

## 🚨 Alerting Rules

### Critical Alerts
- **HighErrorRate**: >10% error rate for 2 minutes
- **GrpcServiceDown**: gRPC service unavailable
- **HighMemoryUsage**: >80% heap memory usage

### Warning Alerts
- **HighResponseTime**: >2s response time (95th percentile)
- **HighCpuUsage**: >80% CPU usage

### Info Alerts
- **NoActiveConnections**: No connections for 10 minutes

## 🔧 Configuration

### Application Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
        step: 10s
    distribution:
      percentiles-histogram:
        http.server.requests: true
        grpc.client.requests: true
```

### Prometheus Configuration
```yaml
scrape_configs:
  - job_name: 'reactive-gateway'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    static_configs:
      - targets: ['host.docker.internal:8080']
```

## 📊 Custom Metrics Implementation

### Service Layer Metrics
```java
@Timed(value = "user.service.get", description = "Time taken to get user by ID")
public Mono<UserDto> getUserById(String userId) {
    Timer.Sample sample = customMetrics.startGrpcTimer();
    // ... service logic
    customMetrics.stopGrpcTimer(sample);
}
```

### Controller Layer Metrics
```java
@Timed(value = "http.requests", extraTags = {"endpoint", "getUserById"})
public Mono<ResponseEntity<UserDto>> getUserById(@PathVariable String id) {
    customMetrics.incrementActiveConnections();
    // ... controller logic
    customMetrics.decrementActiveConnections();
}
```

## 🔍 Health Checks

### Application Health
- **URL**: http://localhost:8080/actuator/health
- **Includes**: 
  - Application status
  - gRPC service connectivity
  - Database connections (if applicable)
  - Custom health indicators

### gRPC Health Indicator
```java
@Component
public class GrpcHealthIndicator implements ReactiveHealthIndicator {
    // Custom health check for gRPC service connectivity
}
```

## 📱 Monitoring Best Practices

### 1. **Metric Naming**
- Use consistent naming conventions
- Include units in metric names
- Use labels for dimensions

### 2. **Alert Thresholds**
- Set realistic thresholds based on SLAs
- Use multiple severity levels
- Avoid alert fatigue

### 3. **Dashboard Design**
- Focus on key business metrics
- Use appropriate visualization types
- Include context and annotations

### 4. **Performance Impact**
- Monitor metrics collection overhead
- Use sampling for high-volume traces
- Optimize metric cardinality

## 🛠️ Troubleshooting

### Common Issues

1. **Metrics Not Appearing**
   - Check application is exposing `/actuator/prometheus`
   - Verify Prometheus can reach the application
   - Check firewall/network connectivity

2. **High Memory Usage**
   - Review metric cardinality
   - Check for metric label explosion
   - Tune retention policies

3. **Missing Traces**
   - Verify Zipkin configuration
   - Check sampling probability
   - Ensure proper instrumentation

### Debug Commands
```bash
# Check application metrics
curl http://localhost:8080/actuator/prometheus

# Check Prometheus targets
curl http://localhost:9090/api/v1/targets

# Test alert rules
curl http://localhost:9090/api/v1/rules
```

## 🔄 Maintenance

### Regular Tasks
- Review and update alert thresholds
- Clean up old metrics and dashboards
- Update monitoring stack versions
- Review and optimize queries

### Backup
- Export Grafana dashboards
- Backup Prometheus data
- Document custom configurations

## 📚 Additional Resources

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Documentation](https://grafana.com/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)