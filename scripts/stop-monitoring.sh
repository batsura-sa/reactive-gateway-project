#!/bin/bash

# Stop monitoring stack for Reactive Gateway
echo "🛑 Stopping Monitoring Stack..."

# Stop and remove containers
docker-compose -f docker-compose.monitoring.yml down

echo "✅ Monitoring stack stopped successfully!"
echo ""
echo "💡 To remove all data volumes, run:"
echo "   docker-compose -f docker-compose.monitoring.yml down -v"