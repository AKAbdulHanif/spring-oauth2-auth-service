# OAuth2 Authorization Service

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)](https://github.com/company/spring-auth-demo)
[![Java Version](https://img.shields.io/badge/java-11-blue)](https://openjdk.java.net/projects/jdk/11/)
[![Spring Boot](https://img.shields.io/badge/spring--boot-2.7.0-green)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

A production-ready OAuth2 Authorization Service built with Spring Boot, implementing OAuth2 Client Credentials flow with multi-tenant support, JWT token generation, and comprehensive security features.

## 🏗️ Architecture Overview

This service provides a complete OAuth2/OIDC authorization server with the following key features:

- **OAuth2 Client Credentials Flow** - Machine-to-machine authentication
- **Multi-Tenant Support** - Isolated client management per tenant
- **JWT Token Generation** - Stateless, secure token implementation
- **Client Registration API** - Dynamic client onboarding
- **Token Introspection** - RFC 7662 compliant token validation
- **JWKS Endpoint** - Public key distribution for token verification
- **Discovery Endpoint** - OAuth2/OIDC metadata publication

### 📊 System Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client Apps   │───▶│   API Gateway   │───▶│ Downstream APIs │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       ▲
         │                       ▼                       │
         │              ┌─────────────────┐              │
         └─────────────▶│ OAuth2 Service  │──────────────┘
                        │                 │
                        │ • Token Gen     │
                        │ • Validation    │
                        │ • Client Mgmt   │
                        │ • Multi-tenant  │
                        └─────────────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │   Data Layer    │
                        │ H2 / PostgreSQL │
                        └─────────────────┘
```

For detailed architecture documentation, see [Architecture Overview](docs/architecture-overview.md).

## 🚀 Quick Start

### Prerequisites

- **Java 11+** - OpenJDK or Oracle JDK
- **Maven 3.6+** - For building the application
- **Docker & Docker Compose** - For containerized deployment
- **Git** - For version control

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/company/spring-auth-demo.git
   cd spring-auth-demo
   ```

2. **Build the application**
   ```bash
   mvn clean package -DskipTests
   ```

3. **Run with Docker Compose**
   ```bash
   docker-compose up --build
   ```

4. **Verify the service**
   ```bash
   curl http://localhost:9000/actuator/health
   ```

### Alternative: Run with Maven

```bash
# Development profile with H2 database
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile (requires PostgreSQL)
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 📋 API Usage

### 1. Generate Access Token

```bash
curl -X POST http://localhost:9000/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "retail-payment-service:retail-secret-2024" \
  -d "grant_type=client_credentials&scope=read:accounts write:transactions"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "read:accounts write:transactions"
}
```

### 2. Validate Token

```bash
curl -X POST http://localhost:9000/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "retail-payment-service:retail-secret-2024" \
  -d "token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 3. Register New Client

```bash
curl -X POST http://localhost:9000/api/clients/register \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "New Payment Service",
    "tenantId": "retail-banking",
    "scopes": ["read:accounts", "write:transactions"]
  }'
```

For complete API documentation, see [API Documentation](docs/api-documentation.md).

## 🏢 Multi-Tenant Architecture

The service supports multiple tenants with isolated client management:

- **retail-banking** - Retail banking services
- **corporate-banking** - Corporate banking services  
- **platform** - Platform-wide services

Each tenant has its own set of clients and scopes, ensuring complete isolation.

## 🔐 Security Features

### Authentication & Authorization
- **Client Credentials Flow** - RFC 6749 compliant
- **HTTP Basic Authentication** - For client authentication
- **BCrypt Password Hashing** - Secure client secret storage
- **JWT Tokens** - Stateless, signed tokens with HMAC-SHA256

### Token Security
- **Short-lived tokens** - 1 hour default expiration
- **Scope-based authorization** - Fine-grained access control
- **Tenant isolation** - Multi-tenant security boundaries
- **Token introspection** - RFC 7662 compliant validation

### Security Headers
- **HTTPS enforcement** - TLS 1.2+ required in production
- **CORS configuration** - Controlled cross-origin access
- **Security headers** - HSTS, X-Frame-Options, etc.

## 📊 Monitoring & Observability

### Health Checks
```bash
# Application health
curl http://localhost:9000/actuator/health

# Detailed health with components
curl http://localhost:9000/actuator/health/details
```

### Metrics
```bash
# All metrics
curl http://localhost:9000/actuator/metrics

# OAuth2 specific metrics
curl http://localhost:9000/actuator/metrics/oauth2.token.requests
curl http://localhost:9000/actuator/metrics/oauth2.token.errors
```

### Logging
- **Structured logging** - JSON format for production
- **Security audit logs** - Authentication and authorization events
- **Performance metrics** - Request timing and throughput
- **Error tracking** - Comprehensive error logging

## 🚀 Deployment

### Docker Deployment

```bash
# Build image
docker build -t company/oauth2-service:latest .

# Run container
docker run -p 9000:9000 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=jdbc:postgresql://db:5432/oauth2 \
  company/oauth2-service:latest
```

### Kubernetes Deployment

```bash
# Apply Kubernetes manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -l app=oauth2-service
kubectl get svc oauth2-service
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `SERVER_PORT` | Server port | `9000` |
| `DATABASE_URL` | Database connection URL | H2 in-memory |
| `JWT_SECRET` | JWT signing secret | Generated |
| `JWT_EXPIRATION` | Token expiration (seconds) | `3600` |
| `LOG_LEVEL` | Logging level | `INFO` |

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### Load Testing
```bash
# Using Apache Bench
ab -n 1000 -c 10 -H "Authorization: Basic $(echo -n 'client:secret' | base64)" \
   -p token-request.txt -T application/x-www-form-urlencoded \
   http://localhost:9000/oauth2/token
```

### Test Coverage
```bash
mvn jacoco:report
open target/site/jacoco/index.html
```

## 📁 Project Structure

```
spring-auth-demo/
├── src/main/java/com/company/platform/
│   ├── AuthServerDemoApplication.java     # Main application class
│   ├── config/
│   │   └── SecurityConfig.java            # Security configuration
│   ├── controller/
│   │   ├── OAuth2Controller.java          # OAuth2 endpoints
│   │   └── ClientRegistrationController.java # Client management
│   ├── entity/
│   │   └── OAuth2Client.java              # JPA entity
│   ├── repository/
│   │   └── OAuth2ClientRepository.java    # Data access layer
│   └── service/
│       ├── OAuth2Service.java             # Business logic
│       └── JwtService.java                # JWT operations
├── src/main/resources/
│   └── application.yml                    # Configuration
├── docs/                                  # Documentation
│   ├── architecture-overview.md
│   ├── api-documentation.md
│   └── diagrams/                          # Architecture diagrams
├── examples/                              # Usage examples
├── k8s/                                   # Kubernetes manifests
├── docker-compose.yml                     # Local development
├── Dockerfile                             # Container image
└── pom.xml                               # Maven configuration
```

## 📚 Documentation

- [Architecture Overview](docs/architecture-overview.md) - System design and components
- [API Documentation](docs/api-documentation.md) - Complete API reference
- [Deployment Guide](docs/deployment-guide.md) - Production deployment
- [Security Guide](docs/security-guide.md) - Security best practices
- [Troubleshooting](TROUBLESHOOTING_REPORT.md) - Common issues and solutions

### Architecture Diagrams

- [System Context Diagram](docs/diagrams/system-context-diagram.svg)
- [Component Diagram](docs/diagrams/component-diagram.svg)
- [OAuth2 Flow Diagram](docs/diagrams/oauth2-flow-diagram.svg)
- [Deployment Diagram](docs/diagrams/deployment-diagram.svg)
- [Data Model Diagram](docs/diagrams/data-model-diagram.svg)

## 🔧 Configuration

### Development Configuration

```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:oauth2db
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
      path: /h2-console

logging:
  level:
    com.company.platform: DEBUG
```

### Production Configuration

```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DATABASE_URL}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5

logging:
  level:
    root: INFO
    com.company.platform: INFO
```

## 🤝 Contributing

1. **Fork the repository**
2. **Create a feature branch** (`git checkout -b feature/amazing-feature`)
3. **Commit your changes** (`git commit -m 'Add amazing feature'`)
4. **Push to the branch** (`git push origin feature/amazing-feature`)
5. **Open a Pull Request**

### Development Guidelines

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Write comprehensive unit tests (minimum 80% coverage)
- Update documentation for new features
- Ensure all CI/CD checks pass

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help

- **Documentation**: Check the [docs/](docs/) directory
- **Issues**: Create a [GitHub issue](https://github.com/company/spring-auth-demo/issues)
- **Email**: platform-team@company.com
- **Slack**: #oauth2-support

### Troubleshooting

Common issues and solutions are documented in [TROUBLESHOOTING_REPORT.md](TROUBLESHOOTING_REPORT.md).

### Status & Monitoring

- **Service Status**: https://status.company.platform
- **Metrics Dashboard**: https://metrics.company.platform/oauth2
- **Log Aggregation**: https://logs.company.platform

---

## 🎯 Roadmap

### Current Version (v1.0)
- ✅ OAuth2 Client Credentials flow
- ✅ Multi-tenant support
- ✅ JWT token generation
- ✅ Client registration API
- ✅ Token introspection
- ✅ JWKS endpoint

### Upcoming Features (v1.1)
- 🔄 Authorization Code flow
- 🔄 Refresh token support
- 🔄 PKCE support
- 🔄 OpenID Connect
- 🔄 Dynamic client registration
- 🔄 Token revocation

### Future Enhancements (v2.0)
- 🔮 Device authorization flow
- 🔮 Federated identity providers
- 🔮 Advanced scoping
- 🔮 Rate limiting per client
- 🔮 Audit trail
- 🔮 Admin dashboard

---

**Built with ❤️ by the Platform Team**

