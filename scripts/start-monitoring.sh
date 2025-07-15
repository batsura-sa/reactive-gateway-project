#!/bin/bash

# Start monitoring stack for Reactive Gateway
echo "🚀 Starting Monitoring Stack for Reactive Gateway..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Create monitoring directory if it doesn't exist
mkdir -p monitoring/grafana/dashboards
mkdir -p monitoring/grafana/provisioning/datasources
mkdir -p monitoring/grafana/provisioning/dashboards

# Start the monitoring stack
echo "📊 Starting Prometheus, Grafana, Zipkin, and AlertManager..."
docker-compose -f docker-compose.monitoring.yml up -d

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 30

# Check service status
echo "🔍 Checking service status..."
docker-compose -f docker-compose.monitoring.yml ps

echo ""
echo "✅ Monitoring stack started successfully!"
echo ""
echo "📊 Access your monitoring tools:"
echo "   • Grafana:      http://localhost:3000 (admin/admin123)"
echo "   • Prometheus:   http://localhost:9090"
echo "   • Zipkin:       http://localhost:9411"
echo "   • AlertManager: http://localhost:9093"
echo ""
echo "🎯 Application endpoints:"
echo "   • Health:       http://localhost:8080/actuator/health"
echo "   • Metrics:      http://localhost:8080/actuator/metrics"
echo "   • Prometheus:   http://localhost:8080/actuator/prometheus"
echo ""
echo "🚀 Start your application with:"
echo "   ./gradlew bootRun --args='--spring.profiles.active=mock-grpc'"