package com.truecarbon.trading.service;

import com.truecarbon.trading.client.UserServiceClient;
import com.truecarbon.trading.domain.*;
import com.truecarbon.trading.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeExecutionService {

    private final TradeRepository     tradeRepo;
    private final OrderRepository     orderRepo;
    private final UserServiceClient   userClient;
    private final NotificationService notifService;
    private final PriceHistoryService priceService;

    @Transactional
    public Trade execute(MatchingEngine.MatchResult match) {
        Order buy  = match.buyOrder();
        Order sell = match.sellOrder();
        BigDecimal credits = match.creditsMatched();
        BigDecimal price   = match.tradePrice();

        fill(buy,  credits); orderRepo.save(buy);
        fill(sell, credits); orderRepo.save(sell);

        Trade trade = Trade.builder()
            .buyOrderId(buy.getId()).sellOrderId(sell.getId())
            .buyerId(buy.getUserId()).sellerId(sell.getUserId())
            .certificateId(buy.getCertificateId())
            .certificateTitle(buy.getCertificateTitle())
            .creditsTraded(credits).tradePrice(price)
            .totalAmount(price.multiply(credits))
            .status("COMPLETED")
            .build();
        tradeRepo.save(trade);
        log.info("Trade completed: id={} credits={} price={}", trade.getId(), credits, price);

        postAsync(trade);
        return trade;
    }

    @Async
    public void postAsync(Trade t) {
        try {
            userClient.creditPortfolio(t.getBuyerId(), t.getCertificateId(),
                t.getCertificateTitle(), t.getCreditsTraded(), t.getTradePrice());
            userClient.debitPortfolio(t.getSellerId(), t.getCertificateId(), t.getCreditsTraded());
            priceService.recordTrade(t.getTradePrice(), t.getCreditsTraded());

            notifService.notify(t.getBuyerId(),
                "BUY matched: " + t.getCreditsTraded() + " tCO₂ of " +
                t.getCertificateTitle() + " at ₹" + t.getTradePrice(),
                "ORDER_MATCHED", t.getId());

            notifService.notify(t.getSellerId(),
                "SELL matched: " + t.getCreditsTraded() + " tCO₂ of " +
                t.getCertificateTitle() + " at ₹" + t.getTradePrice(),
                "ORDER_MATCHED", t.getId());
        } catch (Exception e) {
            log.error("postAsync failed for trade {}: {}", t.getId(), e.getMessage());
        }
    }

    private void fill(Order order, BigDecimal credits) {
        order.setCreditsFilled(order.getCreditsFilled().add(credits));
        order.setStatus(order.getCreditsFilled().compareTo(order.getCreditsRequested()) >= 0
            ? "FILLED" : "PARTIALLY_FILLED");
    }
}
