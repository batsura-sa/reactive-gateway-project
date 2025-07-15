# Redis Setup for Distributed Rate Limiting

This guide explains how to configure Redis for distributed rate limiting in your Reactive Gateway application.

## Quick Start

### 1. Start Redis with Docker

```bash
# Start Redis and Redis Commander
./scripts/setup-redis.sh

# Or manually with docker-compose
docker-compose -f docker-compose.redis.yml up -d
```

### 2. Run Application with Redis

```bash
# With Redis profile
./gradlew bootRun --args='--spring.profiles.active=redis'

# Or for production
./gradlew bootRun --args='--spring.profiles.active=production'
```

### 3. Test Rate Limiting

```bash
# Run automated test
./scripts/test-redis-rate-limiting.sh

# Or manual test
curl -H "X-Forwarded-For: 192.168.1.100" http://localhost:8080/api/users/test
```

## Configuration Profiles

### Development Profile (`application-development.yml`)
- **Rate Limiting**: In-memory (no Redis required)
- **Limits**: Relaxed for development
- **Logging**: Debug level for troubleshooting

```bash
./gradlew bootRun --args='--spring.profiles.active=development'
```

### Redis Profile (`application-redis.yml`)
- **Rate Limiting**: Redis-based
- **Limits**: Moderate for testing
- **Redis**: Local Docker instance

```bash
./gradlew bootRun --args='--spring.profiles.active=redis'
```

### Production Profile (`application-production.yml`)
- **Rate Limiting**: Redis-based with SSL
- **Limits**: Production-grade
- **Security**: Password-protected Redis

```bash
./gradlew bootRun --args='--spring.profiles.active=production'
```

## Environment Variables

### Redis Connection
```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_password
export REDIS_DATABASE=0
export REDIS_SSL=false
```

### Rate Limiting
```bash
export RATE_LIMIT_ENABLED=true
export RATE_LIMIT_USE_REDIS=true
export RATE_LIMIT_GET_USER_BY_ID_CAPACITY=100
```

## Redis Deployment Options

### 1. Local Development (Docker)

```yaml
# docker-compose.redis.yml
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
```

**Start**: `docker-compose -f docker-compose.redis.yml up -d`

### 2. AWS ElastiCache

```yaml
spring:
  data:
    redis:
      host: your-cluster.cache.amazonaws.com
      port: 6379
      ssl: true
      timeout: 5000ms
```

### 3. Azure Cache for Redis

```yaml
spring:
  data:
    redis:
      host: your-cache.redis.cache.windows.net
      port: 6380
      password: ${AZURE_REDIS_KEY}
      ssl: true
```

### 4. Google Cloud Memorystore

```yaml
spring:
  data:
    redis:
      host: 10.0.0.3  # Private IP
      port: 6379
      ssl: false
```

## Redis Configuration

### Basic Redis Config (`redis.conf`)

```conf
# Memory management
maxmemory 256mb
maxmemory-policy allkeys-lru

# Performance
tcp-backlog 511
timeout 0
tcp-keepalive 300

# Security (uncomment for production)
# requirepass your_strong_password

# Persistence (optional for rate limiting)
save 900 1
save 300 10
save 60 10000
```

### Production Redis Config

```conf
# Enhanced security
bind 127.0.0.1
protected-mode yes
requirepass your_strong_password

# SSL/TLS
port 0
tls-port 6379
tls-cert-file /path/to/redis.crt
tls-key-file /path/to/redis.key

# Memory optimization
maxmemory 1gb
maxmemory-policy allkeys-lru

# Logging
loglevel notice
logfile /var/log/redis/redis-server.log
```

## Monitoring Redis

### 1. Redis Commander (Web UI)
- **URL**: http://localhost:8081
- **Features**: Browse keys, monitor memory, execute commands

### 2. Redis CLI Commands

```bash
# Connect to Redis
docker exec -it reactive-gateway-redis redis-cli

# Monitor rate limiting keys
KEYS rate_limit:*

# Check memory usage
INFO memory

# Monitor commands
MONITOR

# Get rate limit bucket info
GET rate_limit:getUserById:192.168.1.100
```

### 3. Application Metrics

```bash
# Rate limit metrics
curl http://localhost:8080/actuator/metrics/rate.limit.exceeded.total

# Redis health
curl http://localhost:8080/actuator/health/redis

# All metrics
curl http://localhost:8080/actuator/prometheus | grep rate_limit
```

## Troubleshooting

### Common Issues

#### 1. Redis Connection Failed
```
Error: Unable to connect to Redis at localhost:6379
```

**Solutions**:
- Check if Redis is running: `docker ps | grep redis`
- Verify port: `netstat -an | grep 6379`
- Check firewall settings
- Verify Redis configuration

#### 2. Authentication Failed
```
Error: NOAUTH Authentication required
```

**Solutions**:
- Set password: `REDIS_PASSWORD=your_password`
- Check Redis config: `requirepass` setting
- Verify credentials in application.yml

#### 3. SSL/TLS Issues
```
Error: SSL connection failed
```

**Solutions**:
- Verify SSL settings: `REDIS_SSL=true`
- Check certificates
- Test with SSL disabled first

#### 4. Rate Limiting Not Working
```
All requests succeed, no rate limiting
```

**Solutions**:
- Check configuration: `curl /api/rate-limit/config`
- Verify Redis keys: `redis-cli KEYS rate_limit:*`
- Check logs for errors
- Ensure `rate-limit.use-redis=true`

### Debug Commands

```bash
# Check Redis connection
redis-cli ping

# Monitor Redis activity
redis-cli monitor

# Check rate limit keys
redis-cli --scan --pattern "rate_limit:*"

# View bucket data
redis-cli get "rate_limit:getUserById:192.168.1.100"

# Check Redis info
redis-cli info replication
redis-cli info memory
redis-cli info stats
```

### Performance Tuning

#### Redis Optimization
```conf
# Increase connection limits
tcp-backlog 511
maxclients 10000

# Optimize memory
hash-max-ziplist-entries 512
hash-max-ziplist-value 64

# Disable slow operations
save ""  # Disable persistence for pure cache
```

#### Application Optimization
```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 32    # Increase for high load
          max-idle: 16      # Keep connections warm
          min-idle: 4       # Minimum connections
          max-wait: 1000ms  # Connection timeout
```

## Security Best Practices

### 1. Authentication
- Always use passwords in production
- Use strong, randomly generated passwords
- Rotate passwords regularly

### 2. Network Security
- Use private networks/VPCs
- Enable SSL/TLS for data in transit
- Restrict access with security groups/firewalls

### 3. Redis Security
```conf
# Disable dangerous commands
rename-command FLUSHDB ""
rename-command FLUSHALL ""
rename-command DEBUG ""
rename-command CONFIG ""

# Bind to specific interfaces
bind 127.0.0.1 10.0.0.5

# Enable protected mode
protected-mode yes
```

### 4. Application Security
- Use environment variables for secrets
- Enable SSL for Redis connections
- Monitor for suspicious activity
- Implement proper logging

## Scaling Considerations

### Single Redis Instance
- **Pros**: Simple setup, low latency
- **Cons**: Single point of failure
- **Use case**: Development, small applications

### Redis Cluster
- **Pros**: High availability, horizontal scaling
- **Cons**: Complex setup, potential hotspots
- **Use case**: Large-scale production

### Redis Sentinel
- **Pros**: Automatic failover, monitoring
- **Cons**: Additional complexity
- **Use case**: High availability requirements

## Backup and Recovery

### Backup Strategy
```bash
# Manual backup
redis-cli BGSAVE

# Automated backup (cron)
0 2 * * * redis-cli BGSAVE
```

### Recovery
```bash
# Stop Redis
docker-compose -f docker-compose.redis.yml stop redis

# Restore data
cp backup.rdb /path/to/redis/data/dump.rdb

# Start Redis
docker-compose -f docker-compose.redis.yml start redis
```

## Next Steps

1. **Test the setup**: Run `./scripts/test-redis-rate-limiting.sh`
2. **Monitor performance**: Check Redis metrics and application logs
3. **Tune configuration**: Adjust rate limits based on usage patterns
4. **Set up alerts**: Monitor Redis health and rate limit violations
5. **Plan for scaling**: Consider Redis Cluster for high-traffic scenarios