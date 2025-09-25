# OAuth2 Authorization Service - Architecture Overview

## Executive Summary

The OAuth2 Authorization Service is a simplified yet comprehensive OAuth2/OIDC authorization server built with Spring Boot. It provides secure authentication and authorization capabilities for enterprise platforms, supporting client credentials flow, JWT token generation, and multi-tenant architecture.

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Enterprise Platform Ecosystem                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐         │
│  │   Client        │    │   API Gateway   │    │   Downstream    │         │
│  │ Applications    │◄──►│   (Optional)    │◄──►│   Services      │         │
│  │                 │    │                 │    │                 │         │
│  │ • Web Apps      │    │ • Load Balancer │    │ • Banking APIs  │         │
│  │ • Mobile Apps   │    │ • Rate Limiting │    │ • Payment APIs  │         │
│  │ • Third-party   │    │ • SSL Term.     │    │ • Account APIs  │         │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘         │
│           │                       │                       ▲                │
│           │                       │                       │                │
│           ▼                       ▼                       │                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │              OAuth2 Authorization Service              │               │
│  │                                                        │               │
│  │  ┌─────────────────┐  ┌─────────────────┐             │               │
│  │  │   OAuth2/OIDC   │  │      JWT        │             │               │
│  │  │   Endpoints     │  │   Generator     │             │               │
│  │  │                 │  │  & Validator    │             │               │
│  │  │ • /oauth2/token │  │                 │             │               │
│  │  │ • /introspect   │  │ • HS256 Signing │             │               │
│  │  │ • /.well-known  │  │ • Claims Mgmt   │             │               │
│  │  └─────────────────┘  └─────────────────┘             │               │
│  │                                                        │               │
│  │  ┌─────────────────┐  ┌─────────────────┐             │               │
│  │  │     Client      │  │   Multi-Tenant  │             │               │
│  │  │  Registration   │  │   Management    │             │               │
│  │  │                 │  │                 │             │               │
│  │  │ • Self-Service  │  │ • Tenant Isol.  │             │               │
│  │  │ • Validation    │  │ • Scope Control │             │               │
│  │  │ • Lifecycle     │  │ • Access Control│             │               │
│  │  └─────────────────┘  └─────────────────┘             │               │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    Data Layer                                      │   │
│  │                                                                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │   │
│  │  │   H2 Database   │  │   PostgreSQL    │  │      Redis      │     │   │
│  │  │   (Development) │  │   (Production)  │  │   (Optional)    │     │   │
│  │  │                 │  │                 │  │                 │     │   │
│  │  │ • In-Memory     │  │ • Persistent    │  │ • Session Cache │     │   │
│  │  │ • Quick Start   │  │ • ACID Compliant│  │ • Token Cache   │     │   │
│  │  │ • Development   │  │ • Backup/Recovery│  │ • Performance   │     │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. OAuth2/OIDC Engine

**Purpose**: Implements OAuth2 client credentials flow and OIDC discovery

**Key Features**:
- Client credentials grant type support
- JWT token generation and validation
- Token introspection endpoint
- OIDC discovery and JWKS endpoints
- Multi-tenant scope management

**Endpoints**:
- `POST /oauth2/token` - Token generation
- `POST /oauth2/introspect` - Token validation
- `GET /.well-known/oauth-authorization-server` - OAuth2 discovery
- `GET /.well-known/jwks.json` - JSON Web Key Set

### 2. Client Registration Service

**Purpose**: Self-service client registration and management

**Key Features**:
- Dynamic client registration
- Client lifecycle management
- Tenant-based isolation
- Scope validation and assignment
- Client status management (ACTIVE, INACTIVE, SUSPENDED)

**Endpoints**:
- `POST /auth/api/v1/clients` - Register new client
- `GET /auth/api/v1/clients` - List all clients
- `GET /auth/api/v1/clients/{clientId}` - Get client details
- `PUT /auth/api/v1/clients/{clientId}` - Update client
- `DELETE /auth/api/v1/clients/{clientId}` - Delete client

### 3. JWT Service

**Purpose**: JWT token generation, validation, and claims management

**Key Features**:
- HS256 symmetric key signing
- Configurable token expiration
- Claims-based authorization
- Token introspection support
- Secure key management

**Token Structure**:
```json
{
  "sub": "client-id",
  "iss": "oauth2-auth-server",
  "iat": 1640995200,
  "exp": 1640998800,
  "client_id": "retail-payment-service",
  "tenant_id": "retail-banking",
  "scope": "read:accounts,write:transactions",
  "token_type": "Bearer"
}
```

### 4. Multi-Tenant Management

**Purpose**: Tenant isolation and scope-based access control

**Key Features**:
- Tenant-based client isolation
- Hierarchical scope management
- Tenant-specific configurations
- Cross-tenant access prevention

**Supported Tenants**:
- `retail-banking` - Retail banking services
- `corporate-banking` - Corporate banking services
- `platform` - Platform-wide services

## Data Architecture

### Entity Relationship Model

```
┌─────────────────────────────────────────────────────────────────┐
│                        OAuth2Client                            │
├─────────────────────────────────────────────────────────────────┤
│ + id: Long (PK)                                                 │
│ + clientId: String (Unique)                                     │
│ + clientSecret: String                                          │
│ + clientName: String                                            │
│ + tenantId: String                                              │
│ + scopes: String (CSV)                                          │
│ + accessTokenValiditySeconds: Integer                           │
│ + status: ClientStatus (ENUM)                                   │
│ + createdAt: Instant                                            │
│ + lastUsedAt: Instant                                           │
└─────────────────────────────────────────────────────────────────┘
```

### Database Configuration

**Development Environment**:
- **Database**: H2 In-Memory
- **URL**: `jdbc:h2:mem:authdb`
- **Features**: Quick startup, no persistence, H2 console enabled

**Production Environment**:
- **Database**: PostgreSQL 13+
- **Features**: ACID compliance, backup/recovery, connection pooling
- **Configuration**: Configurable via Docker Compose

## Security Architecture

### Authentication Flow

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Client    │────►│   OAuth2        │────►│   JWT Token     │
│ Application │     │ Authorization   │     │   Generated     │
│             │     │   Server        │     │                 │
│ 1. POST     │     │ 2. Validate     │     │ 3. Return       │
│ /oauth2/    │     │ Credentials     │     │ Access Token    │
│ token       │     │                 │     │                 │
└─────────────┘     └─────────────────┘     └─────────────────┘
       │                       │                       │
       │                       │                       │
       ▼                       ▼                       ▼
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ Client ID   │     │   Database      │     │   JWT Claims    │
│ Client      │     │   Lookup        │     │                 │
│ Secret      │     │                 │     │ • client_id     │
│ Scope       │     │ • Validate      │     │ • tenant_id     │
│ Grant Type  │     │ • Check Status  │     │ • scope         │
│             │     │ • Verify Secret │     │ • expiration    │
└─────────────┘     └─────────────────┘     └─────────────────┘
```

### Authorization Flow

```
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ Resource    │────►│   JWT Token     │────►│   Access        │
│ Server      │     │   Validation    │     │   Granted       │
│             │     │                 │     │                 │
│ 1. API Call │     │ 2. Verify       │     │ 3. Process      │
│ with Bearer │     │ Signature       │     │ Request         │
│ Token       │     │ & Claims        │     │                 │
└─────────────┘     └─────────────────┘     └─────────────────┘
       │                       │                       │
       │                       │                       │
       ▼                       ▼                       ▼
┌─────────────┐     ┌─────────────────┐     ┌─────────────────┐
│ HTTP Header │     │   Validation    │     │   Scope-based   │
│             │     │                 │     │   Authorization │
│ Authorization:│     │ • Signature     │     │                 │
│ Bearer <JWT>│     │ • Expiration    │     │ • Check Scopes  │
│             │     │ • Claims        │     │ • Tenant Access │
│             │     │ • Issuer        │     │ • Resource Perm │
└─────────────┘     └─────────────────┘     └─────────────────┘
```

## Deployment Architecture

### Container Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           Docker Environment                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                oauth2-auth-server                                  │   │
│  │                                                                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │   │
│  │  │   Spring Boot   │  │   Embedded      │  │   Health        │     │   │
│  │  │   Application   │  │   Tomcat        │  │   Checks        │     │   │
│  │  │                 │  │                 │  │                 │     │   │
│  │  │ • OAuth2 Server │  │ • Port 9000     │  │ • /actuator/    │     │   │
│  │  │ • JWT Service   │  │ • HTTP/HTTPS    │  │   health        │     │   │
│  │  │ • Client Mgmt   │  │ • Connection    │  │ • Readiness     │     │   │
│  │  │               │  │   Pooling       │  │ • Liveness      │     │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                   │                                         │
│                                   ▼                                         │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     Data Layer                                     │   │
│  │                                                                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │   │
│  │  │   PostgreSQL    │  │      Redis      │  │   Volume        │     │   │
│  │  │   Container     │  │   Container     │  │   Mounts        │     │   │
│  │  │                 │  │                 │  │                 │     │   │
│  │  │ • Port 5432     │  │ • Port 6379     │  │ • postgres_data │     │   │
│  │  │ • Persistent    │  │ • Cache Layer   │  │ • redis_data    │     │   │
│  │  │ • ACID Compliant│  │ • Session Store │  │ • Backup Vols   │     │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Kubernetes Deployment (Optional)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Kubernetes Cluster                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      Namespace: oauth2                             │   │
│  │                                                                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │   │
│  │  │   Deployment    │  │     Service     │  │     Ingress     │     │   │
│  │  │                 │  │                 │  │                 │     │   │
│  │  │ • Replicas: 3   │  │ • ClusterIP     │  │ • TLS Term.     │     │   │
│  │  │ • Rolling Update│  │ • Port 9000     │  │ • Load Balancing│     │   │
│  │  │ • Health Checks │  │ • Load Balancer │  │ • Path Routing  │     │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘     │   │
│  │                                                                     │   │
│  │  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐     │   │
│  │  │   ConfigMap     │  │     Secret      │  │   PVC           │     │   │
│  │  │                 │  │                 │  │                 │     │   │
│  │  │ • App Config    │  │ • JWT Secret    │  │ • Database      │     │   │
│  │  │ • Environment   │  │ • DB Credentials│  │ • Persistent    │     │   │
│  │  │ • Logging       │  │ • TLS Certs     │  │ • Storage       │     │   │
│  │  └─────────────────┘  └─────────────────┘  └─────────────────┘     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Technology Stack

### Core Framework
- **Spring Boot 2.7.18** - Application framework
- **Spring Security** - Security framework
- **Spring Data JPA** - Data access layer
- **Spring Web** - REST API framework
- **Spring Actuator** - Monitoring and management

### Security & JWT
- **JJWT (Java JWT)** - JWT token handling
- **BCrypt** - Password encoding
- **HMAC-SHA256** - JWT signing algorithm

### Database
- **H2 Database** - Development (in-memory)
- **PostgreSQL 13+** - Production (persistent)
- **Hibernate** - ORM framework

### Containerization
- **Docker** - Container runtime
- **Docker Compose** - Multi-container orchestration
- **OpenJDK 11** - Java runtime
- **Maven 3.8.6** - Build tool

### Monitoring & Operations
- **Spring Actuator** - Health checks and metrics
- **Logback** - Logging framework
- **Docker Health Checks** - Container monitoring

## Configuration Management

### Application Properties

```yaml
# Core Configuration
server:
  port: 9000
  servlet:
    context-path: /auth

# JWT Configuration
oauth2:
  auth:
    jwt:
      secret: "oauth2-demo-secret-key-2024-very-long-and-secure"
      expiration: 3600000  # 1 hour
      issuer: "oauth2-auth-server"

# Database Configuration
spring:
  datasource:
    url: jdbc:h2:mem:authdb  # Development
    # url: jdbc:postgresql://localhost:5432/oauth2_auth  # Production
  jpa:
    hibernate:
      ddl-auto: create-drop  # Development
      # ddl-auto: validate  # Production
```

### Environment Variables

```bash
# Server Configuration
SERVER_PORT=9000
SPRING_PROFILES_ACTIVE=docker

# Database Configuration
DB_URL=jdbc:postgresql://postgres:5432/oauth2_auth
DB_USERNAME=auth_user
DB_PASSWORD=auth_password

# JWT Configuration
JWT_SECRET=your-secure-secret-key
JWT_EXPIRATION=3600000
JWT_ISSUER=oauth2-auth-server

# JVM Configuration
JAVA_OPTS=-Xmx512m -Xms256m -XX:+UseG1GC
```

## API Documentation

### OAuth2 Endpoints

#### Token Generation
```http
POST /oauth2/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials&
client_id=retail-payment-service&
client_secret=payment-secret-2024&
scope=read:accounts,write:transactions
```

#### Token Introspection
```http
POST /oauth2/introspect
Content-Type: application/x-www-form-urlencoded

token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Client Management Endpoints

#### Register Client
```http
POST /auth/api/v1/clients
Content-Type: application/json

{
  "clientName": "New Payment Service",
  "tenantId": "retail-banking",
  "scopes": ["read:accounts", "write:transactions"],
  "description": "Payment processing service",
  "contactEmail": "dev@yourcompany.com"
}
```

#### List Clients
```http
GET /auth/api/v1/clients
```

## Security Considerations

### Current Implementation
- **JWT Signing**: HMAC-SHA256 with shared secret
- **Client Authentication**: Client ID and secret validation
- **Scope Validation**: Tenant-based scope enforcement
- **Token Expiration**: Configurable token lifetime

### Production Recommendations
- **Asymmetric Keys**: Use RSA/ECDSA for JWT signing
- **Key Rotation**: Implement automated key rotation
- **Rate Limiting**: Add API rate limiting
- **Audit Logging**: Comprehensive security event logging
- **TLS/HTTPS**: Enforce HTTPS in production
- **Secret Management**: Use external secret management (AWS Secrets Manager, HashiCorp Vault)

## Monitoring and Observability

### Health Checks
- **Endpoint**: `/auth/actuator/health`
- **Docker Health Check**: Automated container health monitoring
- **Kubernetes Probes**: Readiness and liveness probes

### Metrics
- **Endpoint**: `/auth/actuator/metrics`
- **JVM Metrics**: Memory, GC, thread usage
- **Application Metrics**: Request counts, response times
- **Custom Metrics**: Token generation rates, client registration events

### Logging
- **Framework**: Logback with structured logging
- **Log Levels**: Configurable per package
- **Audit Events**: Client registration, token generation, authentication failures

## Scalability and Performance

### Horizontal Scaling
- **Stateless Design**: JWT tokens enable stateless scaling
- **Database Connection Pooling**: Efficient database resource usage
- **Container Orchestration**: Kubernetes-ready deployment

### Performance Optimizations
- **JWT Validation**: Local signature verification (no database lookup)
- **Connection Pooling**: Database connection reuse
- **Caching**: Optional Redis integration for session caching
- **JVM Tuning**: G1GC and container-aware settings

## Future Enhancements

### Security Enhancements
- **PKCE Support**: Proof Key for Code Exchange
- **mTLS**: Mutual TLS for client authentication
- **OAuth2.1 Compliance**: Latest OAuth2 specification support
- **FAPI Compliance**: Financial-grade API security

### Feature Enhancements
- **Authorization Code Flow**: Support for web applications
- **Refresh Tokens**: Long-lived refresh token support
- **Consent Management**: User consent and approval flows
- **Admin UI**: Web-based administration interface

### Operational Enhancements
- **Distributed Tracing**: OpenTelemetry integration
- **Advanced Monitoring**: Prometheus and Grafana integration
- **Backup and Recovery**: Automated database backup strategies
- **Multi-Region Deployment**: Geographic distribution support

---

*This documentation provides a comprehensive overview of the OAuth2 Authorization Service architecture. For implementation details, refer to the source code and configuration files.*