package com.truecarbon.trading.api.order;

import com.truecarbon.trading.api.Dtos.*;
import com.truecarbon.trading.domain.Order;
import com.truecarbon.trading.security.TradingUserPrincipal;
import com.truecarbon.trading.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/limit")
    public ResponseEntity<OrderResponse> placeLimitOrder(
            @Valid @RequestBody PlaceLimitOrderRequest req,
            @AuthenticationPrincipal TradingUserPrincipal principal) {

        Order order = orderService.placeLimitOrder(
            principal.getUserId(),
            req.certificateId(),
            req.side(),
            req.priceLimit(),
            req.credits()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @PostMapping("/instant")
    public ResponseEntity<OrderResponse> placeInstantBuy(
            @Valid @RequestBody PlaceInstantBuyRequest req,
            @AuthenticationPrincipal TradingUserPrincipal principal) {

        Order order = orderService.placeInstantBuy(
            principal.getUserId(), req.certificateId(), req.credits());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> myOrders(
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        return ResponseEntity.ok(
            orderService.getMyOrders(principal.getUserId())
                .stream().map(this::toResponse).toList()
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(
            @PathVariable UUID id,
            @AuthenticationPrincipal TradingUserPrincipal principal) {
        return ResponseEntity.ok(
            toResponse(orderService.cancelOrder(id, principal.getUserId())));
    }

    @GetMapping("/book/{certId}")
    public ResponseEntity<OrderBookResponse> orderBook(@PathVariable UUID certId) {
        var view = orderService.getOrderBook(certId);

        // Aggregate orders at same price level
        var bids = aggregateLevels(view.bids());
        var asks = aggregateLevels(view.asks());

        return ResponseEntity.ok(new OrderBookResponse(bids, asks, view.lastPrice()));
    }

    // ── Mappers ───────────────────────────────────────────────────────────────

    private OrderResponse toResponse(Order o) {
        return new OrderResponse(
            o.getId(), o.getCertificateId(), o.getCertificateTitle(),
            o.getSide(), o.getOrderType(), o.getPriceLimit(),
            o.getCreditsRequested(), o.getCreditsFilled(),
            o.getStatus(), o.getCreatedAt(), o.getExpiresAt()
        );
    }

    private List<OrderBookEntryResponse> aggregateLevels(List<Order> orders) {
        return orders.stream()
            .collect(Collectors.groupingBy(Order::getPriceLimit,
                Collectors.toList()))
            .entrySet().stream()
            .map(e -> new OrderBookEntryResponse(
                e.getKey(),
                e.getValue().stream()
                    .map(Order::getRemainingCredits)
                    .reduce(BigDecimal.ZERO, BigDecimal::add),
                e.getValue().size()
            ))
            .sorted(Comparator.comparing(OrderBookEntryResponse::price).reversed())
            .toList();
    }
}
