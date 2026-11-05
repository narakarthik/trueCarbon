package com.truecarbon.user.repository;

import com.truecarbon.user.domain.Certificate;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    List<Certificate> findByStatusOrderByListedAtDesc(String status);

    List<Certificate> findByOwnerIdOrderByListedAtDesc(UUID ownerId);

    @Query("SELECT c FROM Certificate c WHERE c.status = 'VERIFIED' AND c.availableCredits > 0 ORDER BY c.listedAt DESC")
    List<Certificate> findAllVerifiedWithCredits();

    @Query("SELECT c FROM Certificate c WHERE c.id = :id")
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Certificate> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Certificate c SET c.availableCredits = c.availableCredits - :amount, c.version = c.version + 1 WHERE c.id = :id AND c.availableCredits >= :amount")
    int deductAvailableCredits(@Param("id") UUID id, @Param("amount") BigDecimal amount);
}
