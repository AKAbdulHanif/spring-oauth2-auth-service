package com.natwest.platform.auth.entity;

import javax.persistence.*;
import java.time.Instant;

/**
 * OAuth2 Client entity for demo
 */
@Entity
@Table(name = "oauth2_clients")
public class OAuth2Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    @Column(name = "client_secret", nullable = false)
    private String clientSecret;

    @Column(name = "client_name", nullable = false)
    private String clientName;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "scopes")
    private String scopes; // Comma-separated

    @Column(name = "access_token_validity_seconds")
    private Integer accessTokenValiditySeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    // Constructors
    public OAuth2Client() {}

    public OAuth2Client(String clientId, String clientSecret, String clientName, String tenantId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientName = clientName;
        this.tenantId = tenantId;
        this.status = ClientStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.accessTokenValiditySeconds = 3600;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientSecret() { return clientSecret; }
    public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }

    public Integer getAccessTokenValiditySeconds() { return accessTokenValiditySeconds; }
    public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) { 
        this.accessTokenValiditySeconds = accessTokenValiditySeconds; 
    }

    public ClientStatus getStatus() { return status; }
    public void setStatus(ClientStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    // Enums
    public enum ClientStatus {
        ACTIVE, SUSPENDED, DEPRECATED
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

