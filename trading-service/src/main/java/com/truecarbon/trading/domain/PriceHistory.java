package com.truecarbon.trading.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "price_history", schema = "trading_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PriceHistory {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recorded_on", nullable = false, unique = true) 
    private LocalDate recordedOn;

    @Column(name = "open_price",  precision = 14, scale = 2) 
    private BigDecimal openPrice;
    @Column(name = "high_price",  precision = 14, scale = 2) 
    private BigDecimal highPrice;
    @Column(name = "low_price",   precision = 14, scale = 2) 
    private BigDecimal lowPrice;
    @Column(name = "close_price", precision = 14, scale = 2) 
    private BigDecimal closePrice;

    @Column(precision = 18, scale = 4) 
    @Builder.Default 
    private BigDecimal volume = BigDecimal.ZERO;
    @Column(name = "trade_count", nullable = false) 
    @Builder.Default 
    private int tradeCount = 0;

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false) 
    private OffsetDateTime createdAt;
}
