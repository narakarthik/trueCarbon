package com.truecarbon.trading.api;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// ── Order DTOs ────────────────────────────────────────────────────────────────

public class Dtos {

    public record PlaceLimitOrderRequest(
        @NotNull UUID certificateId,
        @NotBlank String side,          // BUY | SELL
        @NotNull @Positive BigDecimal priceLimit,
        @NotNull @Positive BigDecimal credits
    ) {}

    public record PlaceInstantBuyRequest(
        @NotNull UUID certificateId,
        @NotNull @Positive BigDecimal credits
    ) {}

    public record OrderResponse(
        UUID           id,
        UUID           certificateId,
        String         certificateTitle,
        String         side,
        String         orderType,
        BigDecimal     priceLimit,
        BigDecimal     creditsRequested,
        BigDecimal     creditsFilled,
        String         status,
        OffsetDateTime createdAt,
        OffsetDateTime expiresAt
    ) {}

    public record OrderBookEntryResponse(
        BigDecimal price,
        BigDecimal credits,
        int        count
    ) {}

    public record OrderBookResponse(
        java.util.List<OrderBookEntryResponse> bids,
        java.util.List<OrderBookEntryResponse> asks,
        BigDecimal lastPrice
    ) {}

    // ── Trade DTOs ─────────────────────────────────────────────────────────────

    public record TradeResponse(
        UUID           id,
        String         certificateTitle,
        String         side,           // BUY or SELL from caller's perspective
        BigDecimal     creditsTraded,
        BigDecimal     tradePrice,
        BigDecimal     totalAmount,
        String         status,
        OffsetDateTime createdAt
    ) {}

    // ── Price DTOs ────────────────────────────────────────────────────────────

    public record PricePointResponse(
        String     recordedOn,
        BigDecimal openPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal closePrice,
        BigDecimal volume,
        int        tradeCount
    ) {}

    // ── Notification DTOs ─────────────────────────────────────────────────────

    public record NotificationResponse(
        UUID           id,
        String         message,
        String         type,
        boolean        read,
        UUID           refId,
        OffsetDateTime createdAt
    ) {}
}
