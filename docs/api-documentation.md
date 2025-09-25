# OAuth2 Authorization Service - API Documentation

## Overview

The OAuth2 Authorization Service provides a comprehensive set of RESTful APIs for OAuth2/OIDC authentication and authorization. This service implements the OAuth2 Client Credentials flow with multi-tenant support and JWT token generation.

## Base URL

```
Local Development: http://localhost:9000/oauth2
Production: https://oauth2.yourcompany.platform/oauth2
```

## Authentication

All OAuth2 endpoints require HTTP Basic Authentication using client credentials:
- **Username**: `client_id`
- **Password**: `client_secret`

## Content Types

- **Request**: `application/x-www-form-urlencoded` or `application/json`
- **Response**: `application/json`

---

## OAuth2 Endpoints

### 1. Token Endpoint

**Generate Access Token using Client Credentials Grant**

```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic {base64(client_id:client_secret)}

grant_type=client_credentials&scope=read:accounts write:transactions
```

#### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `grant_type` | string | Yes | Must be `client_credentials` |
| `scope` | string | No | Space-separated list of requested scopes |

#### Response

**Success (200 OK)**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read:accounts write:transactions"
}
```

**Error Responses**

```json
// Invalid Client (401 Unauthorized)
{
  "error": "invalid_client",
  "error_description": "Client authentication failed"
}

// Invalid Grant (400 Bad Request)
{
  "error": "unsupported_grant_type",
  "error_description": "Grant type not supported"
}

// Invalid Scope (400 Bad Request)
{
  "error": "invalid_scope",
  "error_description": "Requested scope is invalid"
}

// Inactive Client (403 Forbidden)
{
  "error": "access_denied",
  "error_description": "Client is not active"
}
```

### 2. Token Introspection Endpoint

**Validate and Inspect Access Token**

```http
POST /oauth2/introspect
Content-Type: application/x-www-form-urlencoded
Authorization: Basic {base64(client_id:client_secret)}

token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Request Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `token` | string | Yes | The access token to introspect |
| `token_type_hint` | string | No | Hint about token type (always `access_token`) |

#### Response

**Active Token (200 OK)**
```json
{
  "active": true,
  "client_id": "retail-payment-service",
  "scope": "read:accounts write:transactions",
  "exp": 1640995200,
  "iat": 1640991600,
  "sub": "retail-payment-service",
  "iss": "https://oauth2.yourcompany.platform",
  "tenant_id": "retail-banking",
  "token_type": "Bearer"
}
```

**Inactive Token (200 OK)**
```json
{
  "active": false
}
```

### 3. JWKS Endpoint

**Retrieve JSON Web Key Set for Token Verification**

```http
GET /oauth2/.well-known/jwks.json
```

#### Response

```json
{
  "keys": [
    {
      "kty": "oct",
      "use": "sig",
      "alg": "HS256",
      "k": "base64url-encoded-key"
    }
  ]
}
```

### 4. Discovery Endpoint

**OAuth2/OIDC Discovery Document**

```http
GET /oauth2/.well-known/oauth-authorization-server
```

#### Response

```json
{
  "issuer": "https://oauth2.yourcompany.platform",
  "token_endpoint": "https://oauth2.yourcompany.platform/oauth2/token",
  "introspection_endpoint": "https://oauth2.yourcompany.platform/oauth2/introspect",
  "jwks_uri": "https://oauth2.yourcompany.platform/oauth2/.well-known/jwks.json",
  "grant_types_supported": ["client_credentials"],
  "token_endpoint_auth_methods_supported": ["client_secret_basic"],
  "scopes_supported": [
    "read:accounts",
    "write:transactions",
    "read:audit",
    "admin:clients"
  ],
  "response_types_supported": ["token"],
  "subject_types_supported": ["public"]
}
```

---

## Client Management Endpoints

### 1. Register New Client

**Create a new OAuth2 client**

```http
POST /api/clients/register
Content-Type: application/json

{
  "clientName": "New Payment Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "description": "Service for processing retail payments",
  "contactEmail": "team@yourcompany.com"
}
```

#### Request Body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `clientName` | string | Yes | Human-readable client name (max 255 chars) |
| `tenantId` | string | Yes | Tenant identifier (max 100 chars) |
| `scopes` | array | Yes | List of requested scopes |
| `description` | string | No | Client description (max 500 chars) |
| `contactEmail` | string | No | Contact email for the client |

#### Response

**Success (201 Created)**
```json
{
  "clientId": "new-payment-service-abc123",
  "clientSecret": "generated-secret-xyz789",
  "clientName": "New Payment Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "status": "ACTIVE",
  "accessTokenValiditySeconds": 3600,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

**Error Responses**

```json
// Validation Error (400 Bad Request)
{
  "error": "invalid_request",
  "error_description": "Client name is required",
  "details": {
    "clientName": "must not be blank"
  }
}

// Duplicate Client (409 Conflict)
{
  "error": "client_exists",
  "error_description": "Client with this name already exists for tenant"
}
```

### 2. List Clients

**Retrieve clients for a tenant**

```http
GET /api/clients?tenantId=retail-banking&status=ACTIVE&page=0&size=20
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `tenantId` | string | No | Filter by tenant ID |
| `status` | string | No | Filter by client status |
| `page` | integer | No | Page number (default: 0) |
| `size` | integer | No | Page size (default: 20, max: 100) |

#### Response

```json
{
  "content": [
    {
      "clientId": "retail-payment-service",
      "clientName": "Retail Payment Service",
      "tenantId": "retail-banking",
      "scopes": ["read:accounts", "write:transactions"],
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z",
      "lastUsedAt": "2024-01-15T09:45:00Z"
    }
  ],
  "pageable": {
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

## JWT Token Structure

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "sub": "retail-payment-service",
  "client_id": "retail-payment-service",
  "tenant_id": "retail-banking",
  "scope": "read:accounts write:transactions",
  "iss": "https://oauth2.yourcompany.platform",
  "iat": 1640991600,
  "exp": 1640995200
}
```

### Claims Description

| Claim | Description |
|-------|-------------|
| `sub` | Subject (same as client_id) |
| `client_id` | OAuth2 client identifier |
| `tenant_id` | Multi-tenant identifier |
| `scope` | Granted scopes (space-separated) |
| `iss` | Token issuer |
| `iat` | Issued at timestamp |
| `exp` | Expiration timestamp |

---

## Error Handling

### Standard OAuth2 Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `invalid_request` | 400 | Malformed request |
| `invalid_client` | 401 | Client authentication failed |
| `invalid_grant` | 400 | Invalid grant type |
| `unauthorized_client` | 400 | Client not authorized for grant type |
| `unsupported_grant_type` | 400 | Grant type not supported |
| `invalid_scope` | 400 | Invalid or unknown scope |
| `access_denied` | 403 | Client access denied |
| `server_error` | 500 | Internal server error |

### Custom Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `client_inactive` | 403 | Client is not active |
| `client_suspended` | 403 | Client is suspended |
| `tenant_not_found` | 404 | Tenant does not exist |
| `scope_not_allowed` | 400 | Scope not allowed for client |

---

## Rate Limiting

### Token Endpoint
- **Limit**: 100 requests per minute per client
- **Headers**: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

### Client Registration
- **Limit**: 10 requests per hour per IP
- **Headers**: `X-RateLimit-Limit`, `X-RateLimit-Remaining`, `X-RateLimit-Reset`

---

## Security Considerations

### Client Credentials
- Store client secrets securely (BCrypt hashed in database)
- Use HTTPS for all communications
- Rotate client secrets regularly
- Monitor for suspicious activity

### Token Security
- Tokens are stateless JWT with HMAC-SHA256 signature
- Short-lived tokens (1 hour default)
- Include tenant isolation in token claims
- Validate tokens on every API call

### Scope Management
- Implement principle of least privilege
- Validate scopes against client permissions
- Audit scope usage regularly

---

## Code Examples

### Java (Spring Boot)

```java
// Token Request
RestTemplate restTemplate = new RestTemplate();
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
headers.setBasicAuth(clientId, clientSecret);

MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
body.add("grant_type", "client_credentials");
body.add("scope", "read:accounts write:transactions");

HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
TokenResponse response = restTemplate.postForObject(
    "http://localhost:9000/oauth2/token", 
    request, 
    TokenResponse.class
);
```

### Python

```python
import requests
from requests.auth import HTTPBasicAuth

# Token Request
response = requests.post(
    'http://localhost:9000/oauth2/token',
    auth=HTTPBasicAuth(client_id, client_secret),
    data={
        'grant_type': 'client_credentials',
        'scope': 'read:accounts write:transactions'
    }
)

token_data = response.json()
access_token = token_data['access_token']
```

### Node.js

```javascript
const axios = require('axios');

// Token Request
const response = await axios.post(
  'http://localhost:9000/oauth2/token',
  'grant_type=client_credentials&scope=read:accounts write:transactions',
  {
    auth: {
      username: clientId,
      password: clientSecret
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }
);

const accessToken = response.data.access_token;
```

### cURL

```bash
# Token Request
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client_id:client_secret" \
  -d "grant_type=client_credentials&scope=read:accounts write:transactions"

# Token Introspection
curl -X POST http://localhost:9000/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "client_id:client_secret" \
  -d "token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Client Registration
curl -X POST http://localhost:9000/api/clients/register \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "Test Service",
    "tenantId": "retail-banking",
    "scopes": ["read:accounts"]
  }'
```

---

## Testing

### Health Check

```http
GET /actuator/health
```

### Metrics

```http
GET /actuator/metrics
GET /actuator/metrics/oauth2.token.requests
GET /actuator/metrics/oauth2.token.errors
```

### H2 Console (Development Only)

```
URL: http://localhost:9000/h2-console
JDBC URL: jdbc:h2:mem:oauth2db
Username: sa
Password: (empty)
```

---

## Troubleshooting

### Common Issues

1. **Invalid Client Error**
   - Verify client_id and client_secret
   - Check client status (must be ACTIVE)
   - Ensure proper Base64 encoding for Basic Auth

2. **Invalid Scope Error**
   - Verify requested scopes are registered for client
   - Check scope format (space-separated)
   - Ensure scopes exist in the system

3. **Token Validation Failures**
   - Check token expiration
   - Verify JWT signature
   - Ensure token was issued by this service

4. **Rate Limiting**
   - Check rate limit headers
   - Implement exponential backoff
   - Consider caching tokens

### Debug Logging

Enable debug logging in `application.yml`:

```yaml
logging:
  level:
    com.yourcompany.platform: DEBUG
    org.springframework.security: DEBUG
```

---

## Support

For technical support and questions:
- **Email**: platform-team@yourcompany.com
- **Slack**: #oauth2-support
- **Documentation**: https://docs.yourcompany.platform/oauth2
- **Status Page**: https://status.yourcompany.platform