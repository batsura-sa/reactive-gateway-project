# Redis Rate Limiting - Quick Start Guide

## 🚀 Get Started in 3 Steps

### Step 1: Start Redis
```bash
./scripts/setup-redis.sh
```

### Step 2: Run Application with Redis
```bash
./gradlew bootRun --args='--spring.profiles.active=redis'
```

### Step 3: Test Rate Limiting
```bash
./scripts/test-redis-rate-limiting.sh
```

## 📊 What You Get

### ✅ Distributed Rate Limiting
- **Consistent limits** across multiple application instances
- **Redis-backed** token buckets for reliability
- **Automatic failover** to in-memory if Redis is unavailable

### ✅ Per-Endpoint Configuration
- `GET /api/users/{id}` → 100 req/min
- `POST /api/users` → 20 req/min  
- `PUT /api/users/{id}` → 30 req/min
- `DELETE /api/users/{id}` → 10 req/min
- `GET /api/users` → 50 req/min

### ✅ Production Ready
- **Environment-specific configs** (dev, redis, production)
- **Comprehensive monitoring** with Prometheus metrics
- **Security features** (SSL, authentication, network isolation)

## 🔧 Configuration Profiles

| Profile | Redis | Rate Limits | Use Case |
|---------|-------|-------------|----------|
| `development` | ❌ In-memory | Relaxed | Local dev |
| `redis` | ✅ Local Docker | Moderate | Testing |
| `production` | ✅ Secure Redis | Strict | Production |

## 🌐 Environment Variables

```bash
# Redis Connection
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_password
export REDIS_SSL=false

# Rate Limiting
export RATE_LIMIT_ENABLED=true
export RATE_LIMIT_USE_REDIS=true
```

## 📈 Monitoring

### Web UI
- **Redis Commander**: http://localhost:8081
- **Application Health**: http://localhost:8080/actuator/health
- **Rate Limit Config**: http://localhost:8080/api/rate-limit/config

### Metrics
```bash
# Rate limit violations
curl http://localhost:8080/actuator/metrics/rate.limit.exceeded.total

# Redis health
curl http://localhost:8080/actuator/health/redis

# Prometheus metrics
curl http://localhost:8080/actuator/prometheus | grep rate_limit
```

## 🔍 Troubleshooting

### Redis Not Starting?
```bash
# Check Docker
docker ps | grep redis

# View logs
docker-compose -f docker-compose.redis.yml logs redis
```

### Rate Limiting Not Working?
```bash
# Check configuration
curl http://localhost:8080/api/rate-limit/config

# Check Redis keys
docker exec reactive-gateway-redis redis-cli keys "rate_limit:*"
```

### Application Won't Start?
```bash
# Check Redis connection
docker exec reactive-gateway-redis redis-cli ping

# Run without Redis
./gradlew bootRun --args='--spring.profiles.active=development'
```

## 🚀 Production Deployment

### AWS ElastiCache
```yaml
spring:
  data:
    redis:
      host: your-cluster.cache.amazonaws.com
      port: 6379
      ssl: true
```

### Azure Cache for Redis
```yaml
spring:
  data:
    redis:
      host: your-cache.redis.cache.windows.net
      port: 6380
      password: ${AZURE_REDIS_KEY}
      ssl: true
```

### Google Cloud Memorystore
```yaml
spring:
  data:
    redis:
      host: 10.0.0.3  # Private IP
      port: 6379
```

## 📚 Next Steps

1. **Customize rate limits** in `application-redis.yml`
2. **Set up monitoring** with Grafana dashboards
3. **Configure alerts** for rate limit violations
4. **Scale Redis** with clustering for high availability
5. **Implement user-based** rate limiting with JWT tokens

For detailed setup instructions, see [REDIS_SETUP.md](REDIS_SETUP.md)