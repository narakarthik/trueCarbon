package com.truecarbon.trading.api.trade;

import com.truecarbon.trading.api.Dtos.*;
import com.truecarbon.trading.repository.*;
import com.truecarbon.trading.security.TradingUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

// ── TradeController ───────────────────────────────────────────────────────────

@CrossOrigin
@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeRepository tradeRepo;

    @GetMapping("/my")
    public ResponseEntity<List<TradeResponse>> myTrades(
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        UUID userId = principal.getUserId();
        List<TradeResponse> trades = tradeRepo.findByUser(userId)
            .stream()
            .map(t -> new TradeResponse(
                t.getId(),
                t.getCertificateTitle(),
                t.getBuyerId().equals(userId) ? "BUY" : "SELL",
                t.getCreditsTraded(),
                t.getTradePrice(),
                t.getTotalAmount(),
                t.getStatus(),
                t.getCreatedAt()
            ))
            .toList();
        return ResponseEntity.ok(trades);
    }
}