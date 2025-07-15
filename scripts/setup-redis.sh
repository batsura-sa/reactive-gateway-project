#!/bin/bash

echo "Setting up Redis for Reactive Gateway Rate Limiting"
echo "=================================================="

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Docker and Docker Compose are available"

# Stop any existing Redis containers
echo "🛑 Stopping any existing Redis containers..."
docker-compose -f docker-compose.redis.yml down

# Start Redis with Docker Compose
echo "🚀 Starting Redis..."
docker-compose -f docker-compose.redis.yml up -d

# Wait for Redis to be ready
echo "⏳ Waiting for Redis to be ready..."
sleep 5

# Test Redis connection
echo "🔍 Testing Redis connection..."
if docker exec reactive-gateway-redis redis-cli ping | grep -q PONG; then
    echo "✅ Redis is running and responding to ping"
else
    echo "❌ Redis is not responding. Check the logs:"
    docker-compose -f docker-compose.redis.yml logs redis
    exit 1
fi

# Show Redis info
echo "📊 Redis Information:"
docker exec reactive-gateway-redis redis-cli info server | grep redis_version
docker exec reactive-gateway-redis redis-cli info memory | grep used_memory_human

echo ""
echo "🎉 Redis setup complete!"
echo ""
echo "Redis is now running on:"
echo "  - Redis Server: localhost:6379"
echo "  - Redis Commander (Web UI): http://localhost:8081"
echo ""
echo "Environment variables you can set:"
echo "  - REDIS_HOST=localhost"
echo "  - REDIS_PORT=6379"
echo "  - REDIS_PASSWORD= (empty by default)"
echo "  - REDIS_DATABASE=0"
echo "  - REDIS_SSL=false"
echo ""
echo "To run your application with Redis rate limiting:"
echo "  ./gradlew bootRun --args='--spring.profiles.active=redis'"
echo ""
echo "To stop Redis:"
echo "  docker-compose -f docker-compose.redis.yml down"