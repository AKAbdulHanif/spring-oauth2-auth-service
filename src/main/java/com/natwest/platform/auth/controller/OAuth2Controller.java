package com.natwest.platform.auth.controller;

import com.natwest.platform.auth.service.JwtService;
import com.natwest.platform.auth.service.OAuth2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * OAuth2 Controller for token endpoints
 */
@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private OAuth2Service oauth2Service;

    @Autowired
    private JwtService jwtService;

    /**
     * Token endpoint for client credentials grant
     */
    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam(value = "scope", required = false) String scope) {

        try {
            if (!"client_credentials".equals(grantType)) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "unsupported_grant_type");
                error.put("error_description", "Only client_credentials grant type is supported");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = oauth2Service.clientCredentialsGrant(clientId, clientSecret, scope);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "invalid_client");
            error.put("error_description", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "server_error");
            error.put("error_description", "Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Token introspection endpoint
     */
    @PostMapping(value = "/introspect", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> introspect(@RequestParam("token") String token) {
        try {
            Map<String, Object> response = oauth2Service.introspectToken(token);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("active", false);
            return ResponseEntity.ok(error);
        }
    }

    /**
     * JWKS endpoint (simplified - returns key info)
     */
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        Map<String, Object> jwks = new HashMap<>();
        Map<String, Object> key = new HashMap<>();
        
        key.put("kty", "oct"); // Key type: symmetric
        key.put("alg", "HS256"); // Algorithm
        key.put("use", "sig"); // Usage: signature
        key.put("kid", "natwest-demo-key-1"); // Key ID
        
        jwks.put("keys", new Object[]{key});
        
        return ResponseEntity.ok(jwks);
    }

    /**
     * OAuth2 discovery endpoint
     */
    @GetMapping("/.well-known/oauth-authorization-server")
    public ResponseEntity<Map<String, Object>> discovery() {
        Map<String, Object> metadata = new HashMap<>();
        
        metadata.put("issuer", "http://localhost:9000/auth");
        metadata.put("token_endpoint", "http://localhost:9000/auth/oauth2/token");
        metadata.put("introspection_endpoint", "http://localhost:9000/auth/oauth2/introspect");
        metadata.put("jwks_uri", "http://localhost:9000/auth/.well-known/jwks.json");
        metadata.put("grant_types_supported", new String[]{"client_credentials"});
        metadata.put("token_endpoint_auth_methods_supported", new String[]{"client_secret_post", "client_secret_basic"});
        metadata.put("scopes_supported", new String[]{"read:accounts", "write:transactions", "read:treasury", "write:treasury"});
        
        return ResponseEntity.ok(metadata);
    }
}

