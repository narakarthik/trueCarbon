package com.truecarbon.user.api.internal;

import com.truecarbon.user.api.certificate.CertificateDtos.CertificateResponse;
import com.truecarbon.user.api.portfolio.PortfolioDtos.*;
import com.truecarbon.user.domain.User;
import com.truecarbon.user.repository.UserRepository;
import com.truecarbon.user.service.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Internal REST endpoints consumed only by Trading Service.
 * Protected by X-Internal-Key header — see SecurityConfig.
 * These are NOT exposed to end users.
 */
@CrossOrigin
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserRepository     userRepo;
    private final CertificateService certService;
    private final PortfolioService   portfolioService;

    // Get basic user info (name + email) — used in trade notifications
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> getUser(@PathVariable UUID userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return ResponseEntity.ok(Map.of(
            "id",    user.getId().toString(),
            "name",  user.getName(),
            "email", user.getEmail()
        ));
    }

    // Get certificate details — used when validating a sell order
    @GetMapping("/certificates/{certId}")
    public ResponseEntity<CertificateResponse> getCertificate(@PathVariable UUID certId) {
        return ResponseEntity.ok(certService.getById(certId));
    }

    // Get a user's holding for one certificate — used before placing sell orders
    @GetMapping("/portfolio/{userId}/{certId}")
    public ResponseEntity<HoldingResponse> getHolding(
            @PathVariable UUID userId,
            @PathVariable UUID certId) {
        return ResponseEntity.ok(portfolioService.getHolding(userId, certId));
    }

    // Credit portfolio — called when a BUY trade completes
    @PostMapping("/portfolio/credit")
    public ResponseEntity<Void> credit(@Valid @RequestBody CreditRequest req) {
        portfolioService.credit(req);
        return ResponseEntity.ok().build();
    }

    // Debit portfolio — called when a SELL trade completes
    @PostMapping("/portfolio/debit")
    public ResponseEntity<Void> debit(@Valid @RequestBody DebitRequest req) {
        portfolioService.debit(req);
        return ResponseEntity.ok().build();
    }

    // Reserve credits — called when a SELL limit order is placed
    @PostMapping("/portfolio/reserve")
    public ResponseEntity<Void> reserve(@Valid @RequestBody ReserveRequest req) {
        portfolioService.reserve(req);
        return ResponseEntity.ok().build();
    }

    // Release reserved credits — called when a SELL order is cancelled
    @PostMapping("/portfolio/release")
    public ResponseEntity<Void> release(@Valid @RequestBody ReserveRequest req) {
        portfolioService.release(req);
        return ResponseEntity.ok().build();
    }

    // Deduct available credits from certificate — called after instant buy
    @PostMapping("/certificates/{certId}/deduct")
    public ResponseEntity<Void> deductCertCredits(
            @PathVariable UUID certId,
            @RequestBody Map<String, BigDecimal> body) {
        certService.deductCredits(certId, body.get("credits"));
        return ResponseEntity.ok().build();
    }
}
