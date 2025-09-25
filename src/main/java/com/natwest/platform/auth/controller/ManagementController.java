package com.natwest.platform.auth.controller;

import com.natwest.platform.auth.entity.OAuth2Client;
import com.natwest.platform.auth.repository.OAuth2ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Management Controller with working client registration
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ManagementController {

    @Autowired
    private OAuth2ClientRepository clientRepository;

    /**
     * Registration request DTO
     */
    public static class RegistrationRequest {
        private String clientName;
        private String tenantId;
        private List<String> scopes;
        private String description;
        private String contactEmail;
        private Integer accessTokenValiditySeconds;

        // Getters and setters
        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public List<String> getScopes() { return scopes; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getContactEmail() { return contactEmail; }
        public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

        public Integer getAccessTokenValiditySeconds() { return accessTokenValiditySeconds; }
        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) { 
            this.accessTokenValiditySeconds = accessTokenValiditySeconds; 
        }
    }

    /**
     * Registration response DTO
     */
    public static class RegistrationResponse {
        private String clientId;
        private String clientSecret;
        private String clientName;
        private String tenantId;
        private List<String> scopes;
        private String status;
        private String createdAt;
        private Integer accessTokenValiditySeconds;

        public RegistrationResponse() {}

        // Getters and setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }

        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }

        public String getClientName() { return clientName; }
        public void setClientName(String clientName) { this.clientName = clientName; }

        public String getTenantId() { return tenantId; }
        public void setTenantId(String tenantId) { this.tenantId = tenantId; }

        public List<String> getScopes() { return scopes; }
        public void setScopes(List<String> scopes) { this.scopes = scopes; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public Integer getAccessTokenValiditySeconds() { return accessTokenValiditySeconds; }
        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) { 
            this.accessTokenValiditySeconds = accessTokenValiditySeconds; 
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", "NatWest Authorization Server Demo");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        
        try {
            long clientCount = clientRepository.count();
            health.put("database", "UP");
            health.put("total_clients", clientCount);
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("database_error", e.getMessage());
        }
        
        return ResponseEntity.ok(health);
    }

    /**
     * Application info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "NatWest Platform Authorization Server Demo");
        info.put("description", "OAuth 2.0 Authorization Server with multi-tenant support");
        info.put("version", "1.0.0");
        info.put("features", new String[]{
            "Client Credentials Grant",
            "JWT Token Generation", 
            "Token Introspection",
            "Multi-tenant Support",
            "Self-Service Client Registration"
        });
        info.put("endpoints", Map.of(
            "token", "/oauth2/token",
            "introspect", "/oauth2/introspect", 
            "health", "/api/health",
            "clients", "/api/clients",
            "register", "/api/v1/clients"
        ));
        return ResponseEntity.ok(info);
    }

    /**
     * List all clients (legacy endpoint)
     */
    @GetMapping("/clients")
    public ResponseEntity<Map<String, Object>> listClients() {
        List<OAuth2Client> clients = clientRepository.findAll();
        
        List<Map<String, Object>> clientList = clients.stream().map(client -> {
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("client_id", client.getClientId());
            clientInfo.put("client_name", client.getClientName());
            clientInfo.put("tenant_id", client.getTenantId());
            clientInfo.put("scopes", client.getScopes());
            clientInfo.put("status", client.getStatus().toString());
            clientInfo.put("created_at", client.getCreatedAt().toString());
            clientInfo.put("last_used_at", client.getLastUsedAt() != null ? client.getLastUsedAt().toString() : null);
            return clientInfo;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("total", clientList.size());
        response.put("clients", clientList);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Register a new OAuth2 client - WORKING VERSION
     */
    @PostMapping("/v1/clients")
    public ResponseEntity<RegistrationResponse> registerClient(@RequestBody RegistrationRequest request) {
        try {
            // Validate required fields
            if (request.getClientName() == null || request.getTenantId() == null || request.getScopes() == null) {
                return ResponseEntity.badRequest().build();
            }

            // Generate client ID and secret
            String clientId = generateClientId(request.getClientName(), request.getTenantId());
            String clientSecret = generateClientSecret();

            // Create new OAuth2Client entity
            OAuth2Client client = new OAuth2Client();
            client.setClientId(clientId);
            client.setClientSecret(clientSecret);
            client.setClientName(request.getClientName());
            client.setTenantId(request.getTenantId());
            client.setScopes(String.join(",", request.getScopes()));
            client.setStatus(OAuth2Client.ClientStatus.ACTIVE);
            client.setCreatedAt(Instant.now());
            client.setAccessTokenValiditySeconds(
                request.getAccessTokenValiditySeconds() != null ? 
                request.getAccessTokenValiditySeconds() : 3600
            );

            // Save to database
            OAuth2Client savedClient = clientRepository.save(client);

            // Create response
            RegistrationResponse response = new RegistrationResponse();
            response.setClientId(savedClient.getClientId());
            response.setClientSecret(savedClient.getClientSecret());
            response.setClientName(savedClient.getClientName());
            response.setTenantId(savedClient.getTenantId());
            response.setScopes(Arrays.asList(savedClient.getScopes().split(",")));
            response.setStatus(savedClient.getStatus().toString());
            response.setCreatedAt(savedClient.getCreatedAt().toString());
            response.setAccessTokenValiditySeconds(savedClient.getAccessTokenValiditySeconds());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * List all clients (v1 endpoint)
     */
    @GetMapping("/v1/clients")
    public ResponseEntity<Map<String, Object>> listClientsV1() {
        try {
            List<OAuth2Client> clients = clientRepository.findAll();
            
            List<Map<String, Object>> clientList = new ArrayList<>();
            for (OAuth2Client client : clients) {
                Map<String, Object> clientInfo = new HashMap<>();
                clientInfo.put("clientId", client.getClientId());
                clientInfo.put("clientName", client.getClientName());
                clientInfo.put("tenantId", client.getTenantId());
                clientInfo.put("scopes", Arrays.asList(client.getScopes().split(",")));
                clientInfo.put("status", client.getStatus().toString());
                clientInfo.put("createdAt", client.getCreatedAt().toString());
                clientList.add(clientInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("clients", clientList);
            response.put("totalCount", clients.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Generate client ID based on name and tenant
     */
    private String generateClientId(String clientName, String tenantId) {
        String sanitized = clientName.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-");
        
        return sanitized + "-" + System.currentTimeMillis();
    }

    /**
     * Generate secure client secret
     */
    private String generateClientSecret() {
        return "secret-" + UUID.randomUUID().toString().replace("-", "");
    }
}

