package com.truecarbon.user.api.certificate;

import com.truecarbon.user.api.certificate.CertificateDtos.*;
import com.truecarbon.user.security.UserPrincipal;
import com.truecarbon.user.service.CertificateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certService;

    // Public — browse all verified certificates
    @GetMapping
    public ResponseEntity<List<CertificateResponse>> listAll() {
        return ResponseEntity.ok(certService.listAll());
    }

    // Public — single certificate detail
    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(certService.getById(id));
    }

    // Authenticated — list my own certificates
    @GetMapping("/my")
    public ResponseEntity<List<CertificateResponse>> my(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(certService.getMyCertificates(principal.getId()));
    }

    // Authenticated — submit a new certificate
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CertificateResponse> create(
            @Valid @RequestPart("data") CreateRequest req,
            @RequestPart(value = "certFile", required = false) MultipartFile certFile,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(certService.create(req, certFile, principal.getId()));
    }

    // Authenticated — verify a certificate (stub: auto-approves)
    @PatchMapping("/{id}/verify")
    public ResponseEntity<CertificateResponse> verify(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(certService.verify(id, principal.getId()));
    }
}
