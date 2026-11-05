package com.truecarbon.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "certificates", schema = "user_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_credits", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalCredits;

    @Column(name = "available_credits", nullable = false, precision = 18, scale = 4)
    private BigDecimal availableCredits;

    @Column(name = "registry_id", nullable = false, length = 100)
    private String registryId;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "project_type", length = 100)
    private String projectType;

    @Column(name = "project_location", length = 200)
    private String projectLocation;

    @Column(name = "vintage_year")
    private Short vintageYear;

    @Column(name = "cert_file_url", columnDefinition = "TEXT")
    private String certFileUrl;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(name = "listed_at", updatable = false)
    private OffsetDateTime listedAt;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void deductCredits(BigDecimal amount) {
        if (availableCredits.compareTo(amount) < 0) {
            throw new IllegalStateException(
                "Insufficient credits: available=" + availableCredits + ", requested=" + amount);
        }
        this.availableCredits = this.availableCredits.subtract(amount);
    }
}
