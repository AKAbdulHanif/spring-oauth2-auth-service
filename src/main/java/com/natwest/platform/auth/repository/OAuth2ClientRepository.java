package com.natwest.platform.auth.repository;

import com.natwest.platform.auth.entity.OAuth2Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Long> {

    Optional<OAuth2Client> findByClientId(String clientId);

    List<OAuth2Client> findByTenantId(String tenantId);

    List<OAuth2Client> findByStatus(OAuth2Client.ClientStatus status);

    boolean existsByClientId(String clientId);

    @Modifying
    @Transactional
    @Query("UPDATE OAuth2Client c SET c.lastUsedAt = :timestamp WHERE c.clientId = :clientId")
    void updateLastUsedAt(@Param("clientId") String clientId, @Param("timestamp") Instant timestamp);
}

