package com.truecarbon.trading.service;

import com.truecarbon.trading.client.UserServiceClient;
import com.truecarbon.trading.domain.Order;
import com.truecarbon.trading.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository     orderRepo;
    private final UserServiceClient   userClient;
    private final MatchingEngine      matchingEngine;
    private final TradeExecutionService tradeExecutor;

    @Transactional
    public Order placeLimitOrder(UUID userId, UUID certId, String side,
                                  BigDecimal priceLimit, BigDecimal credits) {
        // Validate certificate exists and is VERIFIED
        var cert = userClient.getCertificate(certId);
        if (cert == null) throw new EntityNotFoundException("Certificate not found");
        if (!"VERIFIED".equals(cert.status()))
            throw new IllegalStateException("Certificate is not verified");

        // For SELL orders — validate user has enough credits and reserve them
        if ("SELL".equals(side)) {
            var holding = userClient.getHolding(userId, certId);
            if (holding == null)
                throw new IllegalStateException("You don't hold any credits for this certificate");
            BigDecimal available = holding.creditsHeld().subtract(holding.creditsReserved());
            if (available.compareTo(credits) < 0)
                throw new IllegalStateException(
                    "Insufficient credits. Available to sell: " + available);
            // Lock the credits so user can't double-sell
            userClient.reserveCredits(userId, certId, credits);
        }

        Order order = Order.builder()
            .userId(userId)
            .certificateId(certId)
            .certificateTitle(cert.title())
            .side(side)
            .orderType("LIMIT")
            .priceLimit(priceLimit)
            .creditsRequested(credits)
            .build();
        orderRepo.save(order);
        log.info("Limit order placed: id={} side={} price={} credits={}", order.getId(), side, priceLimit, credits);

        // Run matching engine immediately
        runMatching(order);
        return orderRepo.findById(order.getId()).orElse(order);
    }

    @Transactional
    public Order placeInstantBuy(UUID userId, UUID certId, BigDecimal credits) {
        var cert = userClient.getCertificate(certId);
        if (cert == null) throw new EntityNotFoundException("Certificate not found");

        // Find cheapest available sell order
        var sellOrders = orderRepo.findCheapestSellOrders(certId);
        if (sellOrders.isEmpty())
            throw new IllegalStateException("No sellers available right now");

        BigDecimal bestPrice = sellOrders.get(0).getPriceLimit();

        Order order = Order.builder()
            .userId(userId)
            .certificateId(certId)
            .certificateTitle(cert.title())
            .side("BUY")
            .orderType("INSTANT")
            .priceLimit(bestPrice) // matches at best available ask
            .creditsRequested(credits)
            .build();
        orderRepo.save(order);

        runMatching(order);
        return orderRepo.findById(order.getId()).orElse(order);
    }

    @Transactional
    public Order cancelOrder(UUID orderId, UUID userId) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (!order.getUserId().equals(userId))
            throw new SecurityException("Not your order");
        if (!order.isOpen())
            throw new IllegalStateException("Order is already " + order.getStatus());

        // Release reserved credits for SELL orders
        if ("SELL".equals(order.getSide())) {
            BigDecimal remaining = order.getRemainingCredits();
            userClient.releaseCredits(userId, order.getCertificateId(), remaining);
        }

        order.setStatus("CANCELLED");
        return orderRepo.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> getMyOrders(UUID userId) {
        return orderRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public OrderBookView getOrderBook(UUID certId) {
        var bids = orderRepo.findOpenBuys(certId);
        var asks = orderRepo.findOpenSells(certId);
        BigDecimal lastPrice = getLastTradePrice(certId);
        return new OrderBookView(bids, asks, lastPrice);
    }

    private void runMatching(Order order) {
        var matches = matchingEngine.match(order);
        for (var match : matches) {
            tradeExecutor.execute(match);
        }
    }

    private BigDecimal getLastTradePrice(UUID certId) {
        // Simple: return the price of the cheapest current ask or BigDecimal.ZERO
        return orderRepo.findCheapestSellOrders(certId)
            .stream().findFirst()
            .map(Order::getPriceLimit)
            .orElse(BigDecimal.ZERO);
    }

    public record OrderBookView(
        List<Order> bids,
        List<Order> asks,
        BigDecimal lastPrice
    ) {}
}
