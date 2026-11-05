package com.truecarbon.user.service;

import com.truecarbon.user.api.portfolio.PortfolioDtos.*;
import com.truecarbon.user.domain.*;
import com.truecarbon.user.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioHoldingRepository holdingRepo;
    private final UserRepository             userRepo;

    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings(UUID userId) {
        return holdingRepo.findByUserIdOrderByCertificateTitleAsc(userId)
            .stream().map(this::toResponse).toList();
    }

    // Called by Trading Service — adds credits after a BUY trade completes
    @Transactional
    public void credit(CreditRequest req) {
        PortfolioHolding holding = getOrCreate(req.userId(), req.certificateId(), req.certificateTitle());

        // Recalculate average buy price
        BigDecimal totalCost = holding.getAvgBuyPrice()
            .multiply(holding.getCreditsHeld())
            .add(req.price().multiply(req.credits()));

        holding.setCreditsHeld(holding.getCreditsHeld().add(req.credits()));

        if (holding.getCreditsHeld().compareTo(BigDecimal.ZERO) > 0) {
            holding.setAvgBuyPrice(
                totalCost.divide(holding.getCreditsHeld(), 2, RoundingMode.HALF_UP)
            );
        }

        holdingRepo.save(holding);
        log.info("Portfolio credited: user={}, cert={}, credits={}", req.userId(), req.certificateId(), req.credits());
    }

    // Called by Trading Service — removes credits after a SELL trade completes
    @Transactional
    public void debit(DebitRequest req) {
        PortfolioHolding holding = holdingRepo
            .findByUserIdAndCertificateId(req.userId(), req.certificateId())
            .orElseThrow(() -> new EntityNotFoundException("Holding not found"));

        if (holding.getAvailableToSell().compareTo(req.credits()) < 0) {
            throw new IllegalStateException("Insufficient available credits to debit");
        }

        holding.setCreditsHeld(holding.getCreditsHeld().subtract(req.credits()));
        holdingRepo.save(holding);
        log.info("Portfolio debited: user={}, cert={}, credits={}", req.userId(), req.certificateId(), req.credits());
    }

    // Called by Trading Service — locks credits when a SELL order is placed
    @Transactional
    public void reserve(ReserveRequest req) {
        PortfolioHolding holding = holdingRepo
            .findByUserIdAndCertificateId(req.userId(), req.certificateId())
            .orElseThrow(() -> new EntityNotFoundException("Holding not found"));

        if (holding.getAvailableToSell().compareTo(req.credits()) < 0) {
            throw new IllegalStateException(
                "Insufficient credits to reserve. Available: " + holding.getAvailableToSell());
        }

        holding.setCreditsReserved(holding.getCreditsReserved().add(req.credits()));
        holdingRepo.save(holding);
        log.info("Credits reserved: user={}, credits={}", req.userId(), req.credits());
    }

    // Called by Trading Service — unlocks credits when a SELL order is cancelled
    @Transactional
    public void release(ReserveRequest req) {
        PortfolioHolding holding = holdingRepo
            .findByUserIdAndCertificateId(req.userId(), req.certificateId())
            .orElseThrow(() -> new EntityNotFoundException("Holding not found"));

        BigDecimal newReserved = holding.getCreditsReserved().subtract(req.credits());
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) newReserved = BigDecimal.ZERO;
        holding.setCreditsReserved(newReserved);
        holdingRepo.save(holding);
        log.info("Credits released: user={}, credits={}", req.userId(), req.credits());
    }

    // Called by Trading Service — check how much a user holds for a specific cert
    @Transactional(readOnly = true)
    public HoldingResponse getHolding(UUID userId, UUID certId) {
        return holdingRepo.findByUserIdAndCertificateId(userId, certId)
            .map(this::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Holding not found"));
    }

    private PortfolioHolding getOrCreate(UUID userId, UUID certId, String certTitle) {
        return holdingRepo.findByUserIdAndCertificateId(userId, certId)
            .orElseGet(() -> {
                User user = userRepo.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
                return PortfolioHolding.builder()
                    .user(user)
                    .certificateId(certId)
                    .certificateTitle(certTitle)
                    .build();
            });
    }

    private HoldingResponse toResponse(PortfolioHolding h) {
        return new HoldingResponse(
            h.getId(), h.getCertificateId(), h.getCertificateTitle(),
            h.getCreditsHeld(), h.getCreditsReserved(), h.getAvgBuyPrice()
        );
    }
}
