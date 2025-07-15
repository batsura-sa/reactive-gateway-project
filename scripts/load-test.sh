#!/bin/bash

# Load testing script to generate metrics for monitoring
echo "🔥 Starting load test for Reactive Gateway..."

# Check if application is running
if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "❌ Application is not running. Please start it first:"
    echo "   ./gradlew bootRun --args='--spring.profiles.active=mock-grpc'"
    exit 1
fi

# Function to create users
create_users() {
    echo "👤 Creating users..."
    for i in {1..20}; do
        curl -s -X POST http://localhost:8080/api/users \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"User $i\",
                \"email\": \"user$i@example.com\",
                \"age\": $((20 + i))
            }" > /dev/null
        echo -n "."
    done
    echo " ✅ Created 20 users"
}

# Function to get users
get_users() {
    echo "📖 Getting users..."
    for i in {1..50}; do
        # Get random user ID (1-20)
        user_id=$((1 + RANDOM % 20))
        curl -s http://localhost:8080/api/users/$user_id > /dev/null
        echo -n "."
    done
    echo " ✅ Retrieved 50 users"
}

# Function to list users
list_users() {
    echo "📋 Listing users..."
    for i in {1..10}; do
        curl -s "http://localhost:8080/api/users?page=0&size=10" > /dev/null
        echo -n "."
    done
    echo " ✅ Listed users 10 times"
}

# Function to update users
update_users() {
    echo "✏️ Updating users..."
    for i in {1..10}; do
        user_id=$((1 + RANDOM % 20))
        curl -s -X PUT http://localhost:8080/api/users/$user_id \
            -H "Content-Type: application/json" \
            -d "{
                \"name\": \"Updated User $user_id\",
                \"email\": \"updated$user_id@example.com\",
                \"age\": $((25 + i))
            }" > /dev/null
        echo -n "."
    done
    echo " ✅ Updated 10 users"
}

# Function to delete some users
delete_users() {
    echo "🗑️ Deleting users..."
    for i in {15..20}; do
        curl -s -X DELETE http://localhost:8080/api/users/$i > /dev/null
        echo -n "."
    done
    echo " ✅ Deleted 6 users"
}

# Function to generate some errors
generate_errors() {
    echo "⚠️ Generating some errors..."
    for i in {1..5}; do
        # Try to get non-existent users
        curl -s http://localhost:8080/api/users/999$i > /dev/null
        # Try to create invalid users
        curl -s -X POST http://localhost:8080/api/users \
            -H "Content-Type: application/json" \
            -d "{\"name\": \"\", \"email\": \"invalid\", \"age\": -1}" > /dev/null
        echo -n "."
    done
    echo " ✅ Generated some 404s and validation errors"
}

# Run load test
echo "🎯 Running comprehensive load test..."
echo "   This will generate various metrics for monitoring"
echo ""

# Run operations in sequence
create_users
sleep 2
get_users
sleep 2
list_users
sleep 2
update_users
sleep 2
delete_users
sleep 2
generate_errors

echo ""
echo "✅ Load test completed!"
echo ""
echo "📊 Check your monitoring dashboards:"
echo "   • Grafana:    http://localhost:3000"
echo "   • Prometheus: http://localhost:9090"
echo "   • Metrics:    http://localhost:8080/actuator/prometheus"
echo ""
echo "🔄 To run continuous load test:"
echo "   while true; do ./scripts/load-test.sh; sleep 10; done"