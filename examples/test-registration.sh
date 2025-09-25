#!/bin/bash

echo "🔧 OAuth2 Client Registration Test Script"
echo "=========================================="

# Configuration
AUTH_SERVER_URL="http://localhost:9000/auth"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to check if server is running
check_server() {
    echo -e "${BLUE}🔍 Checking if authorization server is running...${NC}"
    
    if curl -s -f "$AUTH_SERVER_URL/api/health" > /dev/null; then
        echo -e "${GREEN}✅ Authorization server is running${NC}"
        return 0
    else
        echo -e "${RED}❌ Authorization server is not running${NC}"
        echo "Please start the server first:"
        echo "  ./quick-start.sh"
        return 1
    fi
}

# Function to register a new client
register_client() {
    local client_name="$1"
    local tenant_id="$2"
    local scopes="$3"
    local description="$4"
    
    echo -e "${BLUE}📝 Registering client: $client_name${NC}"
    
    local response=$(curl -s -X POST "$AUTH_SERVER_URL/api/v1/clients" \
        -H 'Content-Type: application/json' \
        -d "{
            \"clientName\": \"$client_name\",
            \"tenantId\": \"$tenant_id\",
            \"scopes\": $scopes,
            \"description\": \"$description\",
            \"contactEmail\": \"platform-team@company.com\",
            \"grantTypes\": [\"client_credentials\"]
        }")
    
    if echo "$response" | jq -e '.clientId' > /dev/null 2>&1; then
        local client_id=$(echo "$response" | jq -r '.clientId')
        local client_secret=$(echo "$response" | jq -r '.clientSecret')
        
        echo -e "${GREEN}✅ Client registered successfully${NC}"
        echo "   Client ID: $client_id"
        echo "   Client Secret: ${client_secret:0:20}..."
        
        # Store credentials for testing
        echo "$client_id:$client_secret" >> /tmp/test_clients.txt
        
        return 0
    else
        echo -e "${RED}❌ Failed to register client${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Function to test token generation
test_token_generation() {
    local client_id="$1"
    local client_secret="$2"
    local scope="$3"
    
    echo -e "${BLUE}🎫 Testing token generation for client: $client_id${NC}"
    
    local response=$(curl -s -X POST "$AUTH_SERVER_URL/oauth2/token" \
        -H 'Content-Type: application/x-www-form-urlencoded' \
        -d "grant_type=client_credentials&client_id=$client_id&client_secret=$client_secret&scope=$scope")
    
    if echo "$response" | jq -e '.access_token' > /dev/null 2>&1; then
        local access_token=$(echo "$response" | jq -r '.access_token')
        local token_type=$(echo "$response" | jq -r '.token_type')
        local expires_in=$(echo "$response" | jq -r '.expires_in')
        
        echo -e "${GREEN}✅ Token generated successfully${NC}"
        echo "   Token Type: $token_type"
        echo "   Expires In: $expires_in seconds"
        echo "   Token: ${access_token:0:50}..."
        
        # Test token introspection
        test_token_introspection "$access_token"
        
        return 0
    else
        echo -e "${RED}❌ Failed to generate token${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Function to test token introspection
test_token_introspection() {
    local access_token="$1"
    
    echo -e "${BLUE}🔍 Testing token introspection${NC}"
    
    local response=$(curl -s -X POST "$AUTH_SERVER_URL/oauth2/introspect" \
        -H 'Content-Type: application/x-www-form-urlencoded' \
        -d "token=$access_token")
    
    if echo "$response" | jq -e '.active' > /dev/null 2>&1; then
        local active=$(echo "$response" | jq -r '.active')
        local client_id=$(echo "$response" | jq -r '.client_id')
        local scope=$(echo "$response" | jq -r '.scope')
        
        if [ "$active" = "true" ]; then
            echo -e "${GREEN}✅ Token is active and valid${NC}"
            echo "   Client ID: $client_id"
            echo "   Scope: $scope"
        else
            echo -e "${YELLOW}⚠️ Token is inactive${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ Failed to introspect token${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Function to list all clients
list_clients() {
    echo -e "${BLUE}📋 Listing all registered clients${NC}"
    
    local response=$(curl -s "$AUTH_SERVER_URL/api/v1/clients")
    
    if echo "$response" | jq -e '.clients' > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Retrieved client list${NC}"
        echo "$response" | jq '.clients[] | {clientId: .clientId, tenantId: .tenantId, scopes: .scopes}'
        return 0
    else
        echo -e "${RED}❌ Failed to retrieve client list${NC}"
        echo "Response: $response"
        return 1
    fi
}

# Main test execution
main() {
    echo "Starting OAuth2 client registration tests..."
    echo ""
    
    # Clean up previous test data
    rm -f /tmp/test_clients.txt
    
    # Check if server is running
    if ! check_server; then
        exit 1
    fi
    
    echo ""
    echo -e "${YELLOW}🧪 Running registration tests...${NC}"
    echo ""
    
    # Test 1: Register a retail banking service
    register_client \
        "Test Retail Payment Service" \
        "retail-banking" \
        "[\"read:accounts\", \"write:transactions\"]" \
        "Test payment processing service for retail banking"
    
    echo ""
    
    # Test 2: Register a corporate banking service
    register_client \
        "Test Corporate Treasury Service" \
        "corporate-banking" \
        "[\"read:treasury\", \"write:treasury\"]" \
        "Test treasury management service for corporate banking"
    
    echo ""
    
    # Test 3: Register a platform service
    register_client \
        "Test Audit Service" \
        "platform" \
        "[\"read:audit\", \"write:audit\"]" \
        "Test audit and compliance monitoring service"
    
    echo ""
    
    # Test token generation for registered clients
    echo -e "${YELLOW}🧪 Testing token generation...${NC}"
    echo ""
    
    if [ -f /tmp/test_clients.txt ]; then
        while IFS=':' read -r client_id client_secret; do
            test_token_generation "$client_id" "$client_secret" "read:accounts"
            echo ""
        done < /tmp/test_clients.txt
    fi
    
    # List all clients
    echo -e "${YELLOW}🧪 Listing all clients...${NC}"
    echo ""
    list_clients
    
    echo ""
    echo -e "${GREEN}🎉 All tests completed!${NC}"
    echo ""
    echo "📝 Test clients created (stored in /tmp/test_clients.txt):"
    if [ -f /tmp/test_clients.txt ]; then
        cat /tmp/test_clients.txt
    fi
    
    echo ""
    echo "🔗 Useful endpoints:"
    echo "   Health: $AUTH_SERVER_URL/api/health"
    echo "   Clients: $AUTH_SERVER_URL/api/clients"
    echo "   Token: $AUTH_SERVER_URL/oauth2/token"
    echo "   JWKS: $AUTH_SERVER_URL/.well-known/jwks.json"
}

# Run the main function
main "$@"

