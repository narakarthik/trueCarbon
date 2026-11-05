package com.truecarbon.user.api.portfolio;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class PortfolioDtos {

    public record HoldingResponse(
        UUID       id,
        UUID       certificateId,
        String     certificateTitle,
        BigDecimal creditsHeld,
        BigDecimal creditsReserved,
        BigDecimal avgBuyPrice
    ) {}

    // Used by Trading Service via /internal endpoints
    public record CreditRequest(
        @NotNull UUID       userId,
        @NotNull UUID       certificateId,
        @NotBlank String    certificateTitle,
        @NotNull @Positive BigDecimal credits,
        @NotNull @Positive BigDecimal price
    ) {}

    public record DebitRequest(
        @NotNull UUID       userId,
        @NotNull UUID       certificateId,
        @NotNull @Positive BigDecimal credits
    ) {}

    public record ReserveRequest(
        @NotNull UUID       userId,
        @NotNull UUID       certificateId,
        @NotNull @Positive BigDecimal credits
    ) {}
}
