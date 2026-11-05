package com.truecarbon.trading.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "trading_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id",          nullable = false) 
    private UUID   userId;
    @Column(name = "certificate_id",   nullable = false) 
    private UUID   certificateId;
    @Column(name = "certificate_title",nullable = false) 
    private String certificateTitle;
    @Column(nullable = false, length = 10)               
    private String side;
    @Column(name = "order_type", nullable = false, length = 10) 
    private String orderType;

    @Column(name = "price_limit", precision = 14, scale = 2)
    private BigDecimal priceLimit;

    @Column(name = "credits_requested", nullable = false, precision = 18, scale = 4)
    private BigDecimal creditsRequested;

    @Column(name = "credits_filled", nullable = false, precision = 18, scale = 4)
    @Builder.Default 
    private BigDecimal creditsFilled = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default 
    private String status = "OPEN";

    @Column(name = "expires_at") 
    private OffsetDateTime expiresAt;

    @CreationTimestamp @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public BigDecimal getRemainingCredits() {
        return creditsRequested.subtract(creditsFilled);
    }

    public boolean isOpen() {
        return "OPEN".equals(status) || "PARTIALLY_FILLED".equals(status);
    }
}
