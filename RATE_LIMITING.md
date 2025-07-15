# Rate Limiting Documentation

## Overview

The Reactive Gateway now includes comprehensive rate limiting functionality using the Token Bucket algorithm with Bucket4j. This implementation provides:

- **Per-endpoint rate limiting** with configurable limits
- **IP-based rate limiting** by default (extensible to user-based)
- **In-memory or Redis-backed** storage for distributed scenarios
- **Reactive/non-blocking** implementation compatible with WebFlux
- **Comprehensive metrics** and monitoring integration
- **Graceful degradation** (fail-open) when rate limiting service fails

## Features

### Token Bucket Algorithm
- Uses token bucket algorithm for smooth rate limiting
- Configurable capacity and refill rate per endpoint
- Supports burst traffic within limits

### Per-Endpoint Configuration
Each API endpoint has its own rate limit configuration:
- `GET /api/users/{id}` - 60 requests/minute
- `POST /api/users` - 10 requests/minute  
- `PUT /api/users/{id}` - 20 requests/minute
- `DELETE /api/users/{id}` - 5 requests/minute
- `GET /api/users` - 30 requests/minute

### Storage Backends
- **In-Memory**: Default, suitable for single-instance deployments
- **Redis**: Distributed rate limiting for multi-instance deployments

### Response Headers
Rate-limited responses include helpful headers:
- `X-RateLimit-Remaining`: Tokens remaining in bucket
- `X-RateLimit-Available`: Currently available tokens
- `X-RateLimit-Retry-After`: Seconds to wait before retry
- `Retry-After`: Standard HTTP retry header

## Configuration

### Basic Configuration (application.yml)

```yaml
rate-limit:
  enabled: true
  strategy: TOKEN_BUCKET
  use-redis: false  # Set to true for distributed rate limiting
  key-prefix: "rate_limit:"
  
  # Default rate limits (fallback)
  capacity: 100
  refill-tokens: 100
  refill-period: PT1M  # 1 minute
  
  # Per-endpoint rate limits
  get-user-by-id:
    capacity: 60
    refill-tokens: 60
    refill-period: PT1M
  
  create-user:
    capacity: 10
    refill-tokens: 10
    refill-period: PT1M
```

### Redis Configuration (for distributed rate limiting)

```yaml
rate-limit:
  use-redis: true

spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
```

### Environment Variables

You can override configuration using environment variables:
- `RATE_LIMIT_ENABLED=true`
- `RATE_LIMIT_USE_REDIS=true`
- `RATE_LIMIT_GET_USER_BY_ID_CAPACITY=100`

## Usage Examples

### Testing Rate Limits

```bash
# Make requests to test rate limiting
for i in {1..15}; do
  curl -w "Status: %{http_code}, Time: %{time_total}s\n" \
       -H "X-Forwarded-For: 192.168.1.100" \
       http://localhost:8080/api/users/test-user
  sleep 1
done
```

### Check Rate Limit Configuration

```bash
curl http://localhost:8080/api/rate-limit/config
```

### Monitor Rate Limit Metrics

```bash
curl http://localhost:8080/actuator/metrics/rate.limit.exceeded.total
```

## API Endpoints

### Rate Limit Management

- `GET /api/rate-limit/config` - View current rate limit configuration
- `GET /api/rate-limit/health` - Check rate limiting service health

### Monitoring

Rate limiting metrics are available via Actuator:
- `/actuator/metrics/rate.limit.exceeded.total` - Total rate limit violations
- `/actuator/prometheus` - Prometheus metrics including rate limiting

## Error Responses

When rate limit is exceeded, the API returns:

```json
{
  "error": "Rate limit exceeded",
  "message": "Too many requests. Please try again later.",
  "status": 429,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

HTTP Status: `429 Too Many Requests`

## Key Resolution

By default, rate limiting is applied per IP address. The key resolver:

1. Checks `X-Forwarded-For` header (for proxy scenarios)
2. Checks `X-Real-IP` header
3. Falls back to remote address
4. Ultimate fallback: "unknown"

### Custom Key Resolution

You can extend `RateLimitKeyResolver` to implement:
- User-based rate limiting (from JWT tokens)
- API key-based rate limiting
- Custom header-based rate limiting

## Monitoring and Alerting

### Metrics Available

- `rate.limit.exceeded.total` - Counter of rate limit violations
- `gateway.active.connections` - Current active connections
- Standard HTTP and gRPC metrics

### Grafana Dashboard

Rate limiting metrics are included in the existing Grafana dashboards:
- Rate limit violation rates
- Per-endpoint rate limiting status
- Redis connection health (if using Redis backend)

### Alerting Rules

Example Prometheus alerting rule:

```yaml
- alert: HighRateLimitViolations
  expr: rate(rate_limit_exceeded_total[5m]) > 10
  for: 2m
  labels:
    severity: warning
  annotations:
    summary: High rate of rate limit violations
    description: "Rate limit violations: {{ $value }} per second"
```

## Performance Considerations

### In-Memory Backend
- **Pros**: Low latency, no external dependencies
- **Cons**: Not shared across instances, lost on restart
- **Use case**: Single-instance deployments, development

### Redis Backend
- **Pros**: Distributed, persistent, shared across instances
- **Cons**: Network latency, external dependency
- **Use case**: Multi-instance production deployments

### Tuning

- **Capacity**: Maximum burst size
- **Refill Rate**: Sustained request rate
- **Refill Period**: Time window for token refill

Example: `capacity=60, refillTokens=60, refillPeriod=PT1M`
- Allows burst of 60 requests
- Sustains 1 request/second (60/minute)
- Refills completely every minute

## Troubleshooting

### Common Issues

1. **Rate limiting not working**
   - Check `rate-limit.enabled=true`
   - Verify filter is registered
   - Check logs for errors

2. **Redis connection issues**
   - Verify Redis is running
   - Check connection configuration
   - Monitor Redis logs

3. **Inconsistent rate limiting**
   - Check if using Redis in multi-instance setup
   - Verify key resolution logic
   - Check for proxy headers

### Debug Logging

Enable debug logging:

```yaml
logging:
  level:
    com.example.ratelimit: DEBUG
```

### Health Checks

- `/api/rate-limit/health` - Rate limiting service health
- `/actuator/health` - Overall application health including Redis (if used)

## Security Considerations

- Rate limiting helps prevent DoS attacks
- Consider implementing different limits for authenticated vs anonymous users
- Monitor for attempts to bypass rate limiting
- Use proper proxy headers in production environments
- Consider implementing CAPTCHA for repeated violations

## Future Enhancements

Potential improvements:
- Sliding window rate limiting
- Adaptive rate limiting based on system load
- User-tier based rate limiting (premium vs free users)
- Geographic rate limiting
- Machine learning-based anomaly detection