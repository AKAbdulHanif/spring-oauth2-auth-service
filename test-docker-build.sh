#!/bin/bash

echo "🔧 Testing Docker Build for OAuth2 Authorization Server"
echo "=================================================="

# Clean any existing containers
echo "🧹 Cleaning up existing containers..."
docker stop oauth2-auth-server 2>/dev/null || true
docker rm oauth2-auth-server 2>/dev/null || true

# Build the Docker image
echo "🏗️ Building Docker image..."
docker build -t oauth2-auth-server:latest .

if [ $? -eq 0 ]; then
    echo "✅ Docker build successful!"
    
    # Run the container
    echo "🚀 Starting container..."
    docker run -d \
        --name oauth2-auth-server \
        -p 9000:9000 \
        oauth2-auth-server:latest
    
    # Wait for application to start
    echo "⏳ Waiting for application to start..."
    sleep 30
    
    # Test the health endpoint
    echo "🧪 Testing health endpoint..."
    curl -f http://localhost:9000/auth/api/health
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Application is running successfully!"
        echo "🔗 Health Check: http://localhost:9000/auth/api/health"
        echo "🔗 API Info: http://localhost:9000/auth/api/info"
        echo "🔗 Clients: http://localhost:9000/auth/api/clients"
        echo ""
        echo "🧪 Test token generation:"
        echo "curl -X POST http://localhost:9000/auth/oauth2/token \\"
        echo "  -H 'Content-Type: application/x-www-form-urlencoded' \\"
        echo "  -d 'grant_type=client_credentials&client_id=retail-payment-service&client_secret=payment-secret-2024&scope=read:accounts'"
    else
        echo "❌ Application health check failed"
        echo "📋 Container logs:"
        docker logs oauth2-auth-server
    fi
else
    echo "❌ Docker build failed"
    exit 1
fi

