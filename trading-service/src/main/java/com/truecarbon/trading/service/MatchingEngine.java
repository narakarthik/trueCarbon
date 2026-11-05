package com.truecarbon.trading.service;

import com.truecarbon.trading.domain.Order;
import com.truecarbon.trading.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public interface MatchingEngine {
    List<MatchResult> match(Order incoming);

    record MatchResult(Order buyOrder, Order sellOrder,
                       BigDecimal creditsMatched, BigDecimal tradePrice) {}
}

@Component
@RequiredArgsConstructor
@Slf4j
class PriceTimePriorityEngine implements MatchingEngine {

    private final OrderRepository orderRepo;

    @Override
    public List<MatchResult> match(Order incoming) {
        return "BUY".equals(incoming.getSide())
            ? matchBuy(incoming)
            : matchSell(incoming);
    }

    private List<MatchResult> matchBuy(Order buy) {
        List<MatchResult> results = new ArrayList<>();
        BigDecimal remaining = buy.getRemainingCredits();

        for (Order sell : orderRepo.findMatchingSellOrders(buy.getCertificateId(), buy.getPriceLimit())) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            if (sell.getUserId().equals(buy.getUserId())) continue;

            BigDecimal fill  = sell.getRemainingCredits().min(remaining);
            BigDecimal price = sell.getPriceLimit();
            results.add(new MatchResult(buy, sell, fill, price));
            remaining = remaining.subtract(fill);
            log.info("BUY match: buy={} sell={} credits={} price={}", buy.getId(), sell.getId(), fill, price);
        }
        return results;
    }

    private List<MatchResult> matchSell(Order sell) {
        List<MatchResult> results = new ArrayList<>();
        BigDecimal remaining = sell.getRemainingCredits();

        for (Order buy : orderRepo.findMatchingBuyOrders(sell.getCertificateId(), sell.getPriceLimit())) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            if (buy.getUserId().equals(sell.getUserId())) continue;

            BigDecimal fill  = buy.getRemainingCredits().min(remaining);
            BigDecimal price = sell.getPriceLimit();
            results.add(new MatchResult(buy, sell, fill, price));
            remaining = remaining.subtract(fill);
            log.info("SELL match: buy={} sell={} credits={} price={}", buy.getId(), sell.getId(), fill, price);
        }
        return results;
    }
}
