#!/bin/bash

echo "Testing Redis-based Rate Limiting"
echo "================================="

# Configuration
BASE_URL="http://localhost:8080"
ENDPOINT="/api/users/test-user-redis"
REQUESTS=25
DELAY=0.1

echo "Testing endpoint: $BASE_URL$ENDPOINT"
echo "Making $REQUESTS requests with ${DELAY}s delay between requests"
echo ""

# Function to make a request and extract relevant info
make_request() {
    local request_num=$1
    local response=$(curl -s -w "HTTP_CODE:%{http_code}|TIME:%{time_total}" \
                         -H "X-Forwarded-For: 192.168.1.100" \
                         "$BASE_URL$ENDPOINT" 2>/dev/null)
    
    local http_code=$(echo "$response" | grep -o "HTTP_CODE:[0-9]*" | cut -d: -f2)
    local time_total=$(echo "$response" | grep -o "TIME:[0-9.]*" | cut -d: -f2)
    
    # Extract rate limit headers if present
    local headers=$(curl -s -I -H "X-Forwarded-For: 192.168.1.100" "$BASE_URL$ENDPOINT" 2>/dev/null)
    local remaining=$(echo "$headers" | grep -i "x-ratelimit-remaining" | cut -d: -f2 | tr -d ' \r')
    local retry_after=$(echo "$headers" | grep -i "retry-after" | cut -d: -f2 | tr -d ' \r')
    
    printf "Request %2d: HTTP %s (%.3fs)" "$request_num" "$http_code" "$time_total"
    
    if [ -n "$remaining" ]; then
        printf " | Remaining: %s" "$remaining"
    fi
    
    if [ -n "$retry_after" ]; then
        printf " | Retry-After: %ss" "$retry_after"
    fi
    
    if [ "$http_code" = "429" ]; then
        printf " ⚠️  RATE LIMITED"
    elif [ "$http_code" = "200" ]; then
        printf " ✅"
    else
        printf " ❓"
    fi
    
    echo ""
    
    return $http_code
}

# Check if application is running
echo "🔍 Checking if application is running..."
if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
    echo "❌ Application is not running on $BASE_URL"
    echo "Please start the application with: ./gradlew bootRun --args='--spring.profiles.active=redis'"
    exit 1
fi

echo "✅ Application is running"
echo ""

# Check Redis connection
echo "🔍 Checking Redis rate limiting configuration..."
redis_config=$(curl -s "$BASE_URL/api/rate-limit/config" | grep -o '"useRedis":[^,]*' | cut -d: -f2)
if [ "$redis_config" = "true" ]; then
    echo "✅ Redis rate limiting is enabled"
else
    echo "⚠️  Redis rate limiting is not enabled (useRedis: $redis_config)"
fi

echo ""
echo "🚀 Starting rate limit test..."
echo ""

# Make requests
rate_limited_count=0
success_count=0

for i in $(seq 1 $REQUESTS); do
    make_request $i
    exit_code=$?
    
    if [ $exit_code -eq 429 ]; then
        ((rate_limited_count++))
    elif [ $exit_code -eq 200 ]; then
        ((success_count++))
    fi
    
    sleep $DELAY
done

echo ""
echo "📊 Test Results:"
echo "  Total requests: $REQUESTS"
echo "  Successful: $success_count"
echo "  Rate limited: $rate_limited_count"
echo "  Success rate: $(( success_count * 100 / REQUESTS ))%"

if [ $rate_limited_count -gt 0 ]; then
    echo "✅ Rate limiting is working correctly!"
else
    echo "⚠️  No rate limiting detected. Check configuration."
fi

echo ""
echo "🔍 Check Redis keys:"
echo "docker exec reactive-gateway-redis redis-cli keys 'rate_limit:*'"

echo ""
echo "📈 View metrics:"
echo "curl $BASE_URL/actuator/metrics/rate.limit.exceeded.total"