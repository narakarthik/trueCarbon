package com.truecarbon.user.repository;

import com.truecarbon.user.domain.PortfolioHolding;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, UUID> {

    List<PortfolioHolding> findByUserIdOrderByCertificateTitleAsc(UUID userId);

    Optional<PortfolioHolding> findByUserIdAndCertificateId(UUID userId, UUID certificateId);

    boolean existsByUserIdAndCertificateId(UUID userId, UUID certificateId);
}
