/**
 * OAuth2 Client Registration Example in Java
 * 
 * This example demonstrates how to:
 * 1. Register a new OAuth2 client
 * 2. Generate access tokens
 * 3. Validate tokens
 * 4. Integrate with Spring Boot applications
 * 
 * Dependencies (Maven):
 * <dependency>
 *     <groupId>org.springframework</groupId>
 *     <artifactId>spring-web</artifactId>
 *     <version>5.3.23</version>
 * </dependency>
 * <dependency>
 *     <groupId>com.fasterxml.jackson.core</groupId>
 *     <artifactId>jackson-databind</artifactId>
 *     <version>2.15.2</version>
 * </dependency>
 */

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OAuth2ClientExample {
    
    private final String authServerUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public OAuth2ClientExample(String authServerUrl) {
        this.authServerUrl = authServerUrl.endsWith("/") ? authServerUrl.substring(0, authServerUrl.length() - 1) : authServerUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Client Registration Request DTO
     */
    public static class ClientRegistrationRequest {
        @JsonProperty("clientName")
        private String clientName;
        
        @JsonProperty("tenantId")
        private String tenantId;
        
        @JsonProperty("scopes")
        private List<String> scopes;
        
        @JsonProperty("description")
        private String description;
        
        @JsonProperty("contactEmail")
        private String contactEmail;
        
        @JsonProperty("grantTypes")
        private List<String> grantTypes;
        
        @JsonProperty("accessTokenValiditySeconds")
        private Integer accessTokenValiditySeconds;
        
        // Constructors
        public ClientRegistrationRequest() {}
        
        public ClientRegistrationRequest(String clientName, String tenantId, List<String> scopes, 
                                       String description, String contactEmail) {
            this.clientName = clientName;
            this.tenantId = tenantId;
            this.scopes = scopes;
            this.description = description;
            this.contactEmail = contactEmail;
            this.grantTypes = Arrays.asList("client_credentials");
            this.accessTokenValiditySeconds = 3600;
        }
        
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
        @JsonProperty("clientId")
        private String clientId;
        
        @JsonProperty("clientSecret")
        private String clientSecret;
        
        @JsonProperty("clientName")
        private String clientName;
        
        @JsonProperty("tenantId")
        private String tenantId;
        
        @JsonProperty("scopes")
        private List<String> scopes;
        
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("createdAt")
        private String createdAt;
        
        @JsonProperty("accessTokenValiditySeconds")
        private Integer accessTokenValiditySeconds;
        
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
     * Token Response DTO
     */
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("expires_in")
        private Integer expiresIn;
        
        @JsonProperty("scope")
        private String scope;
        
        // Getters and setters
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        
        public Integer getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Integer expiresIn) { this.expiresIn = expiresIn; }
        
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }
    
    /**
     * Check if the authorization server is running
     */
    public boolean checkServerHealth() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                authServerUrl + "/api/health", String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Register a new OAuth2 client
     */
    public ClientRegistrationResponse registerClient(ClientRegistrationRequest request) {
        String url = authServerUrl + "/api/v1/clients";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<ClientRegistrationRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<ClientRegistrationResponse> response = restTemplate.postForEntity(
                url, entity, ClientRegistrationResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to register client: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error registering client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get access token using client credentials grant
     */
    public TokenResponse getAccessToken(String clientId, String clientSecret, String scope) {
        String url = authServerUrl + "/oauth2/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "client_credentials");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("scope", scope);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                url, entity, TokenResponse.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting access token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Introspect an access token
     */
    public Map<String, Object> introspectToken(String accessToken) {
        String url = authServerUrl + "/oauth2/introspect";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("token", accessToken);
        
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to introspect token: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error introspecting token: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get JWKS (JSON Web Key Set)
     */
    public Map<String, Object> getJwks() {
        String url = authServerUrl + "/.well-known/jwks.json";
        
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to get JWKS: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error getting JWKS: " + e.getMessage(), e);
        }
    }
    
    /**
     * Main demonstration method
     */
    public static void main(String[] args) {
        System.out.println("🚀 OAuth2 Client Registration Demo (Java)");
        System.out.println("==========================================");
        
        // Initialize client
        OAuth2ClientExample client = new OAuth2ClientExample("http://localhost:9000/auth");
        
        // Check server health
        System.out.println("\n🔍 Checking server health...");
        if (!client.checkServerHealth()) {
            System.err.println("❌ Authorization server is not running!");
            System.err.println("Please start the server first: ./quick-start.sh");
            System.exit(1);
        }
        System.out.println("✅ Authorization server is running");
        
        try {
            // Register a new client
            System.out.println("\n📝 Registering new client...");
            ClientRegistrationRequest request = new ClientRegistrationRequest(
                "Java Integration Service",
                "platform",
                Arrays.asList("read:data", "write:reports", "read:analytics"),
                "Java-based integration and reporting service",
                "java-team@company.com"
            );
            
            ClientRegistrationResponse registrationResponse = client.registerClient(request);
            System.out.println("✅ Client registered successfully");
            System.out.println("   Client ID: " + registrationResponse.getClientId());
            System.out.println("   Client Secret: " + registrationResponse.getClientSecret().substring(0, 20) + "...");
            System.out.println("   Tenant: " + registrationResponse.getTenantId());
            System.out.println("   Scopes: " + String.join(", ", registrationResponse.getScopes()));
            
            // Generate access token
            System.out.println("\n🎫 Generating access token...");
            TokenResponse tokenResponse = client.getAccessToken(
                registrationResponse.getClientId(),
                registrationResponse.getClientSecret(),
                "read:data"
            );
            System.out.println("✅ Token generated successfully");
            System.out.println("   Token Type: " + tokenResponse.getTokenType());
            System.out.println("   Expires In: " + tokenResponse.getExpiresIn() + " seconds");
            System.out.println("   Scope: " + tokenResponse.getScope());
            System.out.println("   Token: " + tokenResponse.getAccessToken().substring(0, 50) + "...");
            
            // Introspect token
            System.out.println("\n🔍 Introspecting token...");
            Map<String, Object> introspection = client.introspectToken(tokenResponse.getAccessToken());
            System.out.println("✅ Token introspection successful");
            System.out.println("   Active: " + introspection.get("active"));
            System.out.println("   Client ID: " + introspection.get("client_id"));
            System.out.println("   Scope: " + introspection.get("scope"));
            
            // Get JWKS
            System.out.println("\n🔑 Retrieving JWKS...");
            Map<String, Object> jwks = client.getJwks();
            System.out.println("✅ JWKS retrieved successfully");
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
            System.out.println("   Keys count: " + (keys != null ? keys.size() : 0));
            
            // Summary
            System.out.println("\n🎉 Demo completed successfully!");
            System.out.println("\n🔗 Useful endpoints:");
            System.out.println("   Health: " + client.authServerUrl + "/api/health");
            System.out.println("   Clients: " + client.authServerUrl + "/api/clients");
            System.out.println("   Token: " + client.authServerUrl + "/oauth2/token");
            System.out.println("   JWKS: " + client.authServerUrl + "/.well-known/jwks.json");
            
        } catch (Exception e) {
            System.err.println("❌ Error during demo: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

/**
 * Spring Boot Service Example
 * 
 * This shows how to integrate the OAuth2 client registration
 * into a Spring Boot application.
 */
/*
@Service
public class OAuth2ClientService {
    
    private final OAuth2ClientExample oauth2Client;
    
    public OAuth2ClientService(@Value("${oauth2.server.url}") String authServerUrl) {
        this.oauth2Client = new OAuth2ClientExample(authServerUrl);
    }
    
    @PostConstruct
    public void registerApplicationClient() {
        // Auto-register this application as an OAuth2 client
        ClientRegistrationRequest request = new ClientRegistrationRequest(
            "My Spring Boot Application",
            "platform",
            Arrays.asList("read:data", "write:data"),
            "Spring Boot application with OAuth2 integration",
            "dev-team@company.com"
        );
        
        try {
            ClientRegistrationResponse response = oauth2Client.registerClient(request);
            // Store credentials securely (e.g., in application properties or vault)
            log.info("Application registered with client ID: {}", response.getClientId());
        } catch (Exception e) {
            log.error("Failed to register application as OAuth2 client", e);
        }
    }
    
    public String getAccessToken(String scope) {
        // Get access token for API calls
        TokenResponse response = oauth2Client.getAccessToken(clientId, clientSecret, scope);
        return response.getAccessToken();
    }
}
*/

