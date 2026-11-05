package com.truecarbon.trading.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "trades", schema = "trading_schema")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Trade {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "buy_order_id",      nullable = false) 
    private UUID buyOrderId;
    @Column(name = "sell_order_id",     nullable = false) 
    private UUID sellOrderId;
    @Column(name = "buyer_id",          nullable = false) 
    private UUID buyerId;
    @Column(name = "seller_id",         nullable = false) 
    private UUID sellerId;
    @Column(name = "certificate_id",    nullable = false) 
    private UUID certificateId;
    @Column(name = "certificate_title", nullable = false) 
    private String certificateTitle;

    @Column(name = "credits_traded", nullable = false, precision = 18, scale = 4)
    private BigDecimal creditsTraded;

    @Column(name = "trade_price",   nullable = false, precision = 14, scale = 2) 
    private BigDecimal tradePrice;
    @Column(name = "total_amount",  nullable = false, precision = 16, scale = 2) 
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 20) 
    @Builder.Default 
    private String status = "MATCHED";

    @CreationTimestamp 
    @Column(name = "created_at", updatable = false) 
    private OffsetDateTime createdAt;
    @UpdateTimestamp   
    @Column(name = "updated_at")                    
    private OffsetDateTime updatedAt;
}
