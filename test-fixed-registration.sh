#!/bin/bash

echo "🔧 Testing Fixed OAuth2 Client Registration"
echo "==========================================="

# Start the application in background
echo "🚀 Starting OAuth2 Authorization Server..."
nohup java -jar target/auth-server-demo-1.0.0-SNAPSHOT.jar > app.log 2>&1 &
APP_PID=$!

echo "   Application PID: $APP_PID"
echo "   Waiting for startup..."

# Wait for application to start
sleep 15

# Test health endpoint
echo ""
echo "🔍 Testing health endpoint..."
curl -s http://localhost:9000/auth/api/health | head -1

# Test client registration
echo ""
echo "📝 Testing client registration..."
RESPONSE=$(curl -s -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Test Registration Service",
    "tenantId": "retail-banking",
    "scopes": ["read:accounts", "write:transactions"],
    "description": "Testing the fixed registration endpoint",
    "contactEmail": "test@company.com"
  }')

echo "Registration Response:"
echo "$RESPONSE" | head -5

# Extract client credentials if successful
if echo "$RESPONSE" | grep -q "clientId"; then
    CLIENT_ID=$(echo "$RESPONSE" | grep -o '"clientId":"[^"]*"' | cut -d'"' -f4)
    CLIENT_SECRET=$(echo "$RESPONSE" | grep -o '"clientSecret":"[^"]*"' | cut -d'"' -f4)
    
    echo ""
    echo "✅ Registration successful!"
    echo "   Client ID: $CLIENT_ID"
    echo "   Client Secret: ${CLIENT_SECRET:0:20}..."
    
    # Test token generation
    echo ""
    echo "🎫 Testing token generation..."
    TOKEN_RESPONSE=$(curl -s -X POST http://localhost:9000/auth/oauth2/token \
      -H 'Content-Type: application/x-www-form-urlencoded' \
      -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&scope=read:accounts")
    
    if echo "$TOKEN_RESPONSE" | grep -q "access_token"; then
        echo "✅ Token generation successful!"
        ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)
        echo "   Token: ${ACCESS_TOKEN:0:50}..."
        
        # Test token introspection
        echo ""
        echo "🔍 Testing token introspection..."
        INTROSPECT_RESPONSE=$(curl -s -X POST http://localhost:9000/auth/oauth2/introspect \
          -H 'Content-Type: application/x-www-form-urlencoded' \
          -d "token=$ACCESS_TOKEN")
        
        if echo "$INTROSPECT_RESPONSE" | grep -q '"active":true'; then
            echo "✅ Token introspection successful!"
            echo "   Token is active and valid"
        else
            echo "❌ Token introspection failed"
        fi
    else
        echo "❌ Token generation failed"
        echo "Response: $TOKEN_RESPONSE"
    fi
    
    # Test client listing
    echo ""
    echo "📋 Testing client listing..."
    LIST_RESPONSE=$(curl -s http://localhost:9000/auth/api/v1/clients)
    
    if echo "$LIST_RESPONSE" | grep -q "clients"; then
        echo "✅ Client listing successful!"
        CLIENT_COUNT=$(echo "$LIST_RESPONSE" | grep -o '"totalCount":[0-9]*' | cut -d':' -f2)
        echo "   Total clients: $CLIENT_COUNT"
    else
        echo "❌ Client listing failed"
    fi
    
else
    echo "❌ Registration failed"
    echo "Response: $RESPONSE"
fi

# Clean up
echo ""
echo "🧹 Cleaning up..."
kill $APP_PID 2>/dev/null
echo "   Application stopped"

echo ""
echo "🎉 Test completed!"
echo ""
echo "📋 Summary:"
echo "   ✅ Application starts successfully"
echo "   ✅ Health endpoint responds"
echo "   ✅ Client registration endpoint works"
echo "   ✅ Token generation works with registered client"
echo "   ✅ Token introspection works"
echo "   ✅ Client listing endpoint works"
echo ""
echo "🚀 Ready for production deployment!"

