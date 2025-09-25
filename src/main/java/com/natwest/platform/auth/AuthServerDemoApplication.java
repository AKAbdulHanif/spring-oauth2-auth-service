package com.natwest.platform.auth;

import com.natwest.platform.auth.entity.OAuth2Client;
import com.natwest.platform.auth.repository.OAuth2ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * NatWest Authorization Server Demo Application
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.natwest.platform.auth")
public class AuthServerDemoApplication implements CommandLineRunner {

    @Autowired
    private OAuth2ClientRepository clientRepository;

    public static void main(String[] args) {
        System.out.println("Starting NatWest Authorization Server Demo...");
        SpringApplication.run(AuthServerDemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Create sample clients if they don't exist
        createSampleClientIfNotExists(
            "retail-payment-service",
            "payment-secret-2024",
            "Payment Service",
            "retail-banking",
            "read:accounts,write:transactions"
        );

        createSampleClientIfNotExists(
            "corporate-treasury-service",
            "treasury-secret-2024",
            "Treasury Service",
            "corporate-banking",
            "read:treasury,write:treasury"
        );

        createSampleClientIfNotExists(
            "platform-audit-service",
            "audit-secret-2024",
            "Audit Service",
            "platform",
            "read:audit,write:audit"
        );

        System.out.println("\n=== NatWest Authorization Server Demo Started ===");
        System.out.println("Server URL: http://localhost:9000/auth");
        System.out.println("Health Check: http://localhost:9000/auth/api/health");
        System.out.println("API Info: http://localhost:9000/auth/api/info");
        System.out.println("Clients List: http://localhost:9000/auth/api/clients");
        System.out.println("H2 Console: http://localhost:9000/auth/h2-console");
        System.out.println("JWKS Endpoint: http://localhost:9000/auth/.well-known/jwks.json");
        System.out.println("OAuth2 Discovery: http://localhost:9000/auth/.well-known/oauth-authorization-server");
        System.out.println("\n=== Sample Token Request ===");
        System.out.println("curl -X POST http://localhost:9000/auth/oauth2/token \\");
        System.out.println("  -H 'Content-Type: application/x-www-form-urlencoded' \\");
        System.out.println("  -d 'grant_type=client_credentials&client_id=retail-payment-service&client_secret=payment-secret-2024&scope=read:accounts'");
        System.out.println("================================================\n");
    }

    private void createSampleClientIfNotExists(String clientId, String clientSecret, String clientName, 
                                             String tenantId, String scopes) {
        if (!clientRepository.existsByClientId(clientId)) {
            OAuth2Client client = new OAuth2Client(clientId, clientSecret, clientName, tenantId);
            client.setScopes(scopes);
            client.setAccessTokenValiditySeconds(3600);
            clientRepository.save(client);
            System.out.println("Created sample client: " + clientId + " (tenant: " + tenantId + ")");
        }
    }
}

