package com.natwest.platform.auth.controller;

import com.natwest.platform.auth.entity.OAuth2Client;
import com.natwest.platform.auth.repository.OAuth2ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/auth/api/v1/clients")
@CrossOrigin(origins = "*")
public class ClientRegistrationController {

    @Autowired
    private OAuth2ClientRepository clientRepository;

    /**
     * Client Registration Request DTO
     */
    public static class ClientRegistrationRequest {
        private String clientName;
        private String tenantId;
        private List<String> scopes;
        private String description;
        private String contactEmail;
        private List<String> grantTypes;
        private Integer accessTokenValiditySeconds;

        // Constructors
        public ClientRegistrationRequest() {}

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

        public List<String> getGrantTypes() { return grantTypes; }
        public void setGrantTypes(List<String> grantTypes) { this.grantTypes = grantTypes; }

        public Integer getAccessTokenValiditySeconds() { return accessTokenValiditySeconds; }
        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) { 
            this.accessTokenValiditySeconds = accessTokenValiditySeconds; 
        }
    }

    /**
     * Client Registration Response DTO
     */
    public static class ClientRegistrationResponse {
        private String clientId;
        private String clientSecret;
        private String clientName;
        private String tenantId;
        private List<String> scopes;
        private String status;
        private String createdAt;
        private Integer accessTokenValiditySeconds;

        // Constructors
        public ClientRegistrationResponse() {}

        public ClientRegistrationResponse(OAuth2Client client) {
            this.clientId = client.getClientId();
            this.clientSecret = client.getClientSecret();
            this.clientName = client.getClientName();
            this.tenantId = client.getTenantId();
            this.scopes = Arrays.asList(client.getScopes().split(","));
            this.status = client.getStatus().toString();
            this.createdAt = client.getCreatedAt().toString();
            this.accessTokenValiditySeconds = client.getAccessTokenValiditySeconds();
        }

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
     * Register a new OAuth2 client
     */
    @PostMapping
    public ResponseEntity<ClientRegistrationResponse> registerClient(@RequestBody ClientRegistrationRequest request) {
        try {
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

            // Return response
            ClientRegistrationResponse response = new ClientRegistrationResponse(savedClient);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * List all clients
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listClients() {
        try {
            List<OAuth2Client> clients = clientRepository.findAll();
            
            List<Map<String, Object>> clientList = new ArrayList<>();
            for (OAuth2Client client : clients) {
                Map<String, Object> clientInfo = new HashMap<>();
                clientInfo.put("clientId", client.getClientId());
                clientInfo.put("clientName", client.getClientName());
                clientInfo.put("tenantId", client.getTenantId());
                clientInfo.put("scopes", Arrays.asList(client.getScopes().split(",")));
                clientInfo.put("status", client.getStatus());
                clientInfo.put("createdAt", client.getCreatedAt().toString());
                clientInfo.put("lastUsedAt", client.getLastUsedAt() != null ? client.getLastUsedAt().toString() : null);
                clientList.add(clientInfo);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("clients", clientList);
            response.put("totalCount", clients.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get client details by ID
     */
    @GetMapping("/{clientId}")
    public ResponseEntity<ClientRegistrationResponse> getClient(@PathVariable String clientId) {
        try {
            Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
            
            if (clientOpt.isPresent()) {
                ClientRegistrationResponse response = new ClientRegistrationResponse(clientOpt.get());
                // Don't return the secret in GET requests
                response.setClientSecret("***");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update client
     */
    @PutMapping("/{clientId}")
    public ResponseEntity<ClientRegistrationResponse> updateClient(
            @PathVariable String clientId, 
            @RequestBody ClientRegistrationRequest request) {
        try {
            Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
            
            if (clientOpt.isPresent()) {
                OAuth2Client client = clientOpt.get();
                
                // Update fields
                if (request.getClientName() != null) {
                    client.setClientName(request.getClientName());
                }
                if (request.getScopes() != null) {
                    client.setScopes(String.join(",", request.getScopes()));
                }
                if (request.getAccessTokenValiditySeconds() != null) {
                    client.setAccessTokenValiditySeconds(request.getAccessTokenValiditySeconds());
                }

                OAuth2Client savedClient = clientRepository.save(client);
                ClientRegistrationResponse response = new ClientRegistrationResponse(savedClient);
                // Don't return the secret in update responses
                response.setClientSecret("***");
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deactivate client
     */
    @DeleteMapping("/{clientId}")
    public ResponseEntity<Map<String, Object>> deactivateClient(@PathVariable String clientId) {
        try {
            Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
            
            if (clientOpt.isPresent()) {
                OAuth2Client client = clientOpt.get();
                client.setStatus(OAuth2Client.ClientStatus.DEPRECATED);
                clientRepository.save(client);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Client " + clientId + " has been deactivated");
                response.put("clientId", clientId);
                response.put("status", "INACTIVE");
                response.put("deactivatedAt", Instant.now().toString());

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
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

