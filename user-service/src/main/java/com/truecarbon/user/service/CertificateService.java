package com.truecarbon.user.service;

import com.truecarbon.user.api.certificate.CertificateDtos.*;
import com.truecarbon.user.domain.Certificate;
import com.truecarbon.user.domain.User;
import com.truecarbon.user.repository.CertificateRepository;
import com.truecarbon.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificateService {

    private final CertificateRepository certRepo;
    private final UserRepository        userRepo;

    @Transactional
    public CertificateResponse create(CreateRequest req, MultipartFile file, UUID ownerId) {
        User owner = userRepo.findById(ownerId)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // File stored locally for now — replace with S3 later
        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = saveFileLocally(file, ownerId);
        }

        Certificate cert = Certificate.builder()
            .owner(owner)
            .title(req.title())
            .description(req.description())
            .totalCredits(req.totalCredits())
            .availableCredits(req.totalCredits())
            .registryId(req.registryId())
            .projectType(req.projectType())
            .projectLocation(req.projectLocation())
            .vintageYear(req.vintageYear())
            .certFileUrl(fileUrl)
            .status("VERIFIED")
            .build();

        certRepo.save(cert);
        log.info("Certificate created: id={}, owner={}", cert.getId(), ownerId);
        return toResponse(cert);
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> listAll() {
        return certRepo.findByStatusOrderByListedAtDesc("VERIFIED")
            .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CertificateResponse getById(UUID id) {
        return certRepo.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Certificate not found"));
    }

    @Transactional(readOnly = true)
    public List<CertificateResponse> getMyCertificates(UUID ownerId) {
        return certRepo.findByOwnerIdOrderByListedAtDesc(ownerId)
            .stream().map(this::toResponse).toList();
    }

    // Verification stub — auto-approves for now
    // Replace with actual govt API call later
    @Transactional
    public CertificateResponse verify(UUID id, UUID requesterId) {
        Certificate cert = certRepo.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Certificate not found"));

        // Stub: auto-verify any certificate
        cert.setStatus("VERIFIED");
        cert.setVerifiedAt(OffsetDateTime.now());
        certRepo.save(cert);

        log.info("Certificate verified (stub): id={}", id);
        return toResponse(cert);
    }

    // Called by Trading Service via InternalController
    @Transactional
    public void deductCredits(UUID certId, BigDecimal amount) {
        int updated = certRepo.deductAvailableCredits(certId, amount);
        if (updated == 0) {
            throw new IllegalStateException("Insufficient credits or certificate not found");
        }
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    public CertificateResponse toResponse(Certificate c) {
        return new CertificateResponse(
            c.getId(), c.getTitle(), c.getDescription(),
            c.getTotalCredits(), c.getAvailableCredits(),
            c.getRegistryId(), c.getStatus(),
            c.getProjectType(), c.getProjectLocation(), c.getVintageYear(),
            c.getCertFileUrl(),
            c.getOwner().getName(), c.getOwner().getId(),
            c.getListedAt(), c.getVerifiedAt()
        );
    }

    // ── Local file store (swap with S3Service later) ──────────────────────────

    private String saveFileLocally(MultipartFile file, UUID ownerId) {
        try {
            Path uploadDir = Paths.get("uploads/certificates");
            Files.createDirectories(uploadDir);
            String filename = ownerId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path dest = uploadDir.resolve(filename);
            file.transferTo(dest);
            return "/uploads/certificates/" + filename;
        } catch (IOException e) {
            log.warn("File upload failed, continuing without file: {}", e.getMessage());
            return null;
        }
    }
}
