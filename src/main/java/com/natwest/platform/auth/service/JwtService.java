package com.natwest.platform.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Service for token generation and validation
 */
@Service
public class JwtService {

    @Value("${natwest.auth.jwt.secret}")
    private String jwtSecret;

    @Value("${natwest.auth.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${natwest.auth.jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate JWT token for client
     */
    public String generateToken(String clientId, String tenantId, String scopes) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("client_id", clientId);
        claims.put("tenant_id", tenantId);
        claims.put("scope", scopes);
        claims.put("token_type", "Bearer");
        
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtExpirationMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(clientId)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extract claims from JWT token
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract client ID from token
     */
    public String extractClientId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("client_id", String.class);
    }

    /**
     * Extract tenant ID from token
     */
    public String extractTenantId(String token) {
        Claims claims = extractClaims(token);
        return claims.get("tenant_id", String.class);
    }

    /**
     * Extract scopes from token
     */
    public String extractScopes(String token) {
        Claims claims = extractClaims(token);
        return claims.get("scope", String.class);
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * Get token expiration time in seconds
     */
    public long getExpirationTimeInSeconds() {
        return jwtExpirationMs / 1000;
    }
}

