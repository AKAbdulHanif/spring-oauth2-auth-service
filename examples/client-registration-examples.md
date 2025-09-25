# Client Registration Examples

This document provides comprehensive examples for registering OAuth2 clients with the Authorization Server.

## 📋 **Registration API Overview**

The Authorization Server provides a REST API for self-service client registration, allowing tenants to register their microservices dynamically.

**Base URL**: `http://localhost:9000/auth/api/v1/clients`

## 🔧 **Registration Endpoints**

### 1. Register New Client

**Endpoint**: `POST /auth/api/v1/clients`

**Request Body**:
```json
{
  "clientName": "Payment Processing Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "description": "Handles payment processing for retail banking customers",
  "contactEmail": "platform-team@company.com",
  "redirectUris": [],
  "grantTypes": ["client_credentials"],
  "tokenEndpointAuthMethod": "client_secret_basic"
}
```

**Response**:
```json
{
  "clientId": "retail-payment-service-2024",
  "clientSecret": "generated-secret-abc123xyz",
  "clientName": "Payment Processing Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "status": "ACTIVE",
  "createdAt": "2024-09-24T10:30:00Z",
  "expiresAt": null,
  "accessTokenValiditySeconds": 3600
}
```

### 2. List All Clients

**Endpoint**: `GET /auth/api/v1/clients`

**Response**:
```json
{
  "clients": [
    {
      "clientId": "retail-payment-service",
      "clientName": "Retail Payment Service",
      "tenantId": "retail-banking",
      "scopes": ["read:accounts", "write:transactions"],
      "status": "ACTIVE",
      "createdAt": "2024-09-24T10:00:00Z",
      "lastUsedAt": "2024-09-24T10:45:00Z"
    },
    {
      "clientId": "corporate-treasury-service",
      "clientName": "Corporate Treasury Service",
      "tenantId": "corporate-banking",
      "scopes": ["read:treasury", "write:treasury"],
      "status": "ACTIVE",
      "createdAt": "2024-09-24T10:15:00Z",
      "lastUsedAt": "2024-09-24T10:30:00Z"
    }
  ],
  "totalCount": 2
}
```

### 3. Get Client Details

**Endpoint**: `GET /auth/api/v1/clients/{clientId}`

**Response**:
```json
{
  "clientId": "retail-payment-service",
  "clientName": "Retail Payment Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "status": "ACTIVE",
  "createdAt": "2024-09-24T10:00:00Z",
  "lastUsedAt": "2024-09-24T10:45:00Z",
  "accessTokenValiditySeconds": 3600,
  "description": "Handles payment processing for retail banking customers",
  "contactEmail": "platform-team@company.com"
}
```

### 4. Update Client

**Endpoint**: `PUT /auth/api/v1/clients/{clientId}`

**Request Body**:
```json
{
  "clientName": "Enhanced Payment Processing Service",
  "scopes": ["read:accounts", "write:transactions", "read:customer-profiles"],
  "description": "Enhanced payment processing with customer profile access",
  "contactEmail": "updated-team@company.com"
}
```

### 5. Deactivate Client

**Endpoint**: `DELETE /auth/api/v1/clients/{clientId}`

**Response**:
```json
{
  "message": "Client retail-payment-service has been deactivated",
  "clientId": "retail-payment-service",
  "status": "INACTIVE",
  "deactivatedAt": "2024-09-24T11:00:00Z"
}
```

## 🧪 **cURL Examples**

### Register a New Microservice

```bash
# Register a new account service
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Account Management Service",
    "tenantId": "retail-banking",
    "scopes": ["read:accounts", "write:accounts", "read:customer-data"],
    "description": "Manages customer account operations",
    "contactEmail": "account-team@company.com",
    "grantTypes": ["client_credentials"]
  }'
```

### Register a Cross-Tenant Service

```bash
# Register a service that needs cross-tenant access
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Fraud Detection Service",
    "tenantId": "platform",
    "scopes": ["read:transactions", "read:accounts", "write:alerts"],
    "description": "Cross-tenant fraud detection and monitoring",
    "contactEmail": "security-team@company.com",
    "grantTypes": ["client_credentials"],
    "crossTenantPermissions": [
      {
        "targetTenant": "retail-banking",
        "allowedScopes": ["read:transactions", "read:accounts"]
      },
      {
        "targetTenant": "corporate-banking", 
        "allowedScopes": ["read:transactions"]
      }
    ]
  }'
```

### Register a Reporting Service

```bash
# Register a reporting service with read-only access
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Business Intelligence Service",
    "tenantId": "platform",
    "scopes": ["read:reports", "read:analytics"],
    "description": "Business intelligence and reporting service",
    "contactEmail": "bi-team@company.com",
    "grantTypes": ["client_credentials"],
    "accessTokenValiditySeconds": 7200
  }'
```

## 🔄 **Complete Registration Workflow**

### Step 1: Register the Client

```bash
# Register new client
RESPONSE=$(curl -s -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Order Processing Service",
    "tenantId": "retail-banking",
    "scopes": ["read:orders", "write:orders", "read:inventory"],
    "description": "Processes customer orders",
    "contactEmail": "orders-team@company.com"
  }')

echo "Registration Response:"
echo $RESPONSE | jq .

# Extract client credentials
CLIENT_ID=$(echo $RESPONSE | jq -r '.clientId')
CLIENT_SECRET=$(echo $RESPONSE | jq -r '.clientSecret')

echo "Client ID: $CLIENT_ID"
echo "Client Secret: $CLIENT_SECRET"
```

### Step 2: Test Token Generation

```bash
# Generate access token using new credentials
curl -X POST http://localhost:9000/auth/oauth2/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "grant_type=client_credentials&client_id=$CLIENT_ID&client_secret=$CLIENT_SECRET&scope=read:orders"
```

### Step 3: Validate Token

```bash
# Get the access token from previous response
ACCESS_TOKEN="eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."

# Introspect the token
curl -X POST http://localhost:9000/auth/oauth2/introspect \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d "token=$ACCESS_TOKEN"
```

## 🏢 **Multi-Tenant Registration Examples**

### Retail Banking Tenant

```bash
# Register retail banking services
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Mobile Banking App Backend",
    "tenantId": "retail-banking",
    "scopes": ["read:accounts", "read:transactions", "write:transfers"],
    "description": "Backend service for mobile banking application",
    "contactEmail": "mobile-team@company.com"
  }'
```

### Corporate Banking Tenant

```bash
# Register corporate banking services
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Corporate Treasury Platform",
    "tenantId": "corporate-banking",
    "scopes": ["read:treasury", "write:treasury", "read:fx-rates"],
    "description": "Corporate treasury management platform",
    "contactEmail": "treasury-team@company.com"
  }'
```

### Platform Services

```bash
# Register platform-level services
curl -X POST http://localhost:9000/auth/api/v1/clients \
  -H 'Content-Type: application/json' \
  -d '{
    "clientName": "Audit and Compliance Service",
    "tenantId": "platform",
    "scopes": ["read:audit", "write:audit", "read:compliance"],
    "description": "Audit trail and compliance monitoring",
    "contactEmail": "compliance-team@company.com",
    "crossTenantPermissions": [
      {
        "targetTenant": "retail-banking",
        "allowedScopes": ["read:audit"]
      },
      {
        "targetTenant": "corporate-banking",
        "allowedScopes": ["read:audit"]
      }
    ]
  }'
```

## 🔒 **Security Considerations**

### Client Secret Management

```bash
# Rotate client secret
curl -X POST http://localhost:9000/auth/api/v1/clients/retail-payment-service/rotate-secret \
  -H 'Content-Type: application/json'

# Response includes new secret
{
  "clientId": "retail-payment-service",
  "newClientSecret": "new-generated-secret-xyz789",
  "oldSecretValidUntil": "2024-09-25T10:00:00Z",
  "rotatedAt": "2024-09-24T10:00:00Z"
}
```

### Scope Validation

```bash
# Request token with invalid scope
curl -X POST http://localhost:9000/auth/oauth2/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials&client_id=retail-payment-service&client_secret=payment-secret-2024&scope=admin:all'

# Expected error response
{
  "error": "invalid_scope",
  "error_description": "Requested scope 'admin:all' is not allowed for this client"
}
```

## 📊 **Monitoring and Management**

### Client Usage Statistics

```bash
# Get client usage statistics
curl http://localhost:9000/auth/api/v1/clients/retail-payment-service/stats

# Response
{
  "clientId": "retail-payment-service",
  "totalTokensIssued": 1250,
  "lastTokenIssuedAt": "2024-09-24T10:45:00Z",
  "averageTokensPerDay": 125,
  "mostUsedScopes": ["read:accounts", "write:transactions"],
  "errorRate": 0.02
}
```

### Health Check for Registered Clients

```bash
# Check health of all registered clients
curl http://localhost:9000/auth/api/v1/clients/health

# Response
{
  "totalClients": 5,
  "activeClients": 4,
  "inactiveClients": 1,
  "clientsWithErrors": 0,
  "lastHealthCheck": "2024-09-24T10:50:00Z"
}
```

## 🚀 **Integration Examples**

### Spring Boot Integration

```java
@Service
public class OAuth2ClientService {
    
    @Value("${oauth2.server.url}")
    private String authServerUrl;
    
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<ClientRegistrationRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(
            authServerUrl + "/api/v1/clients", 
            entity, 
            ClientRegistrationResponse.class
        );
    }
    
    public String getAccessToken(String clientId, String clientSecret, String scope) {
        // Implementation for token retrieval
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", scope);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        TokenResponse response = restTemplate.postForObject(
            authServerUrl + "/oauth2/token",
            entity,
            TokenResponse.class
        );
        
        return response.getAccessToken();
    }
}
```

### Python Integration

```python
import requests
import json

class OAuth2Client:
    def __init__(self, auth_server_url):
        self.auth_server_url = auth_server_url
    
    def register_client(self, client_data):
        """Register a new OAuth2 client"""
        url = f"{self.auth_server_url}/api/v1/clients"
        headers = {'Content-Type': 'application/json'}
        
        response = requests.post(url, json=client_data, headers=headers)
        response.raise_for_status()
        
        return response.json()
    
    def get_access_token(self, client_id, client_secret, scope):
        """Get access token using client credentials"""
        url = f"{self.auth_server_url}/oauth2/token"
        
        data = {
            'grant_type': 'client_credentials',
            'client_id': client_id,
            'client_secret': client_secret,
            'scope': scope
        }
        
        response = requests.post(url, data=data)
        response.raise_for_status()
        
        return response.json()['access_token']

# Usage example
client = OAuth2Client('http://localhost:9000/auth')

# Register new client
registration_data = {
    'clientName': 'Python Data Service',
    'tenantId': 'platform',
    'scopes': ['read:data', 'write:reports'],
    'description': 'Python-based data processing service',
    'contactEmail': 'data-team@company.com'
}

result = client.register_client(registration_data)
print(f"Registered client: {result['clientId']}")

# Get access token
token = client.get_access_token(
    result['clientId'], 
    result['clientSecret'], 
    'read:data'
)
print(f"Access token: {token}")
```

## 📝 **Registration Best Practices**

### 1. Naming Conventions

```bash
# Good client names
"retail-payment-service"
"corporate-treasury-api"
"platform-audit-service"

# Avoid
"service1"
"test-client"
"temp-service"
```

### 2. Scope Design

```json
{
  "scopes": [
    "read:accounts",      // Specific resource and action
    "write:transactions", // Clear permission level
    "read:customer-data"  // Descriptive resource name
  ]
}
```

### 3. Security Configuration

```json
{
  "accessTokenValiditySeconds": 3600,  // 1 hour for high-security
  "grantTypes": ["client_credentials"], // Only what's needed
  "tokenEndpointAuthMethod": "client_secret_basic" // Secure auth method
}
```

This comprehensive guide provides everything needed to implement client registration in your OAuth2 Authorization Server deployment.

