package com.natwest.platform.auth.service;

import com.natwest.platform.auth.entity.OAuth2Client;
import com.natwest.platform.auth.repository.OAuth2ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 Service for handling client credentials flow
 */
@Service
public class OAuth2Service {

    @Autowired
    private OAuth2ClientRepository clientRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Handle client credentials grant
     */
    public Map<String, Object> clientCredentialsGrant(String clientId, String clientSecret, String scope) {
        // Validate client credentials
        Optional<OAuth2Client> clientOpt = clientRepository.findByClientId(clientId);
        if (!clientOpt.isPresent()) {
            throw new IllegalArgumentException("Invalid client credentials");
        }

        OAuth2Client client = clientOpt.get();
        
        // Check client status
        if (client.getStatus() != OAuth2Client.ClientStatus.ACTIVE) {
            throw new IllegalArgumentException("Client is not active");
        }

        // Validate client secret (in demo, we store plain text for simplicity)
        if (!client.getClientSecret().equals(clientSecret)) {
            throw new IllegalArgumentException("Invalid client credentials");
        }

        // Validate requested scopes
        String allowedScopes = client.getScopes();
        String grantedScopes = validateAndFilterScopes(scope, allowedScopes);

        // Generate JWT token
        String accessToken = jwtService.generateToken(clientId, client.getTenantId(), grantedScopes);

        // Update last used timestamp
        clientRepository.updateLastUsedAt(clientId, Instant.now());

        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("access_token", accessToken);
        response.put("token_type", "Bearer");
        response.put("expires_in", jwtService.getExpirationTimeInSeconds());
        response.put("scope", grantedScopes);
        response.put("tenant_id", client.getTenantId());

        return response;
    }

    /**
     * Validate and filter requested scopes against allowed scopes
     */
    private String validateAndFilterScopes(String requestedScopes, String allowedScopes) {
        if (requestedScopes == null || requestedScopes.trim().isEmpty()) {
            return allowedScopes; // Return all allowed scopes if none requested
        }

        if (allowedScopes == null || allowedScopes.trim().isEmpty()) {
            return ""; // No scopes allowed
        }

        String[] requested = requestedScopes.split("[,\\s]+");
        String[] allowed = allowedScopes.split("[,\\s]+");

        StringBuilder granted = new StringBuilder();
        for (String requestedScope : requested) {
            for (String allowedScope : allowed) {
                if (requestedScope.trim().equals(allowedScope.trim())) {
                    if (granted.length() > 0) {
                        granted.append(",");
                    }
                    granted.append(requestedScope.trim());
                    break;
                }
            }
        }

        return granted.toString();
    }

    /**
     * Introspect token (validate and return token info)
     */
    public Map<String, Object> introspectToken(String token) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (jwtService.validateToken(token) && !jwtService.isTokenExpired(token)) {
                response.put("active", true);
                response.put("client_id", jwtService.extractClientId(token));
                response.put("tenant_id", jwtService.extractTenantId(token));
                response.put("scope", jwtService.extractScopes(token));
                response.put("token_type", "Bearer");
            } else {
                response.put("active", false);
            }
        } catch (Exception e) {
            response.put("active", false);
        }

        return response;
    }
}

