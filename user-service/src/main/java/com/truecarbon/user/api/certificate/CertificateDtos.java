package com.truecarbon.user.api.certificate;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CertificateDtos {

    public record CreateRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull @Positive BigDecimal totalCredits,
        @NotBlank @Size(max = 100) String registryId,
        String projectType,
        String projectLocation,
        Short vintageYear
    ) {}

    public record CertificateResponse(
        UUID           id,
        String         title,
        String         description,
        BigDecimal     totalCredits,
        BigDecimal     availableCredits,
        String         registryId,
        String         status,
        String         projectType,
        String         projectLocation,
        Short          vintageYear,
        String         certFileUrl,
        String         ownerName,
        UUID           ownerId,
        OffsetDateTime listedAt,
        OffsetDateTime verifiedAt
    ) {}
}
