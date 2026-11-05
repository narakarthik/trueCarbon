package com.truecarbon.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "portfolio_holdings", schema = "user_schema",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "certificate_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "certificate_id", nullable = false)
    private UUID certificateId;

    @Column(name = "certificate_title", nullable = false, length = 200)
    private String certificateTitle;

    @Column(name = "credits_held", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal creditsHeld = BigDecimal.ZERO;

    @Column(name = "credits_reserved", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal creditsReserved = BigDecimal.ZERO;

    @Column(name = "avg_buy_price", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public BigDecimal getAvailableToSell() {
        return creditsHeld.subtract(creditsReserved);
    }
}
