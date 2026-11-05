package com.truecarbon.trading.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.truecarbon.trading.domain.PriceHistory;
import com.truecarbon.trading.repository.PriceHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceHistoryService {

    private final PriceHistoryRepository priceRepo;

    private final AtomicReference<BigDecimal> todayFirst  = new AtomicReference<>(null);
    private final AtomicReference<BigDecimal> todayHigh   = new AtomicReference<>(null);
    private final AtomicReference<BigDecimal> todayLow    = new AtomicReference<>(null);
    private final AtomicReference<BigDecimal> todayLast   = new AtomicReference<>(null);
    private final AtomicReference<BigDecimal> todayVolume = new AtomicReference<>(BigDecimal.ZERO);
    private final AtomicInteger todayCount = new AtomicInteger(0);

    public void recordTrade(BigDecimal price, BigDecimal volume) {
        todayFirst.compareAndSet(null, price);
        todayLast.set(price);
        todayHigh.updateAndGet(h -> h == null ? price : h.max(price));
        todayLow.updateAndGet(l  -> l == null ? price : l.min(price));
        todayVolume.updateAndGet(v -> v.add(volume));
        todayCount.incrementAndGet();
    }

    @Scheduled(cron = "0 55 23 * * *")
    @Transactional
    public void flushDailySnapshot() {
        if (todayCount.get() == 0) return;
        LocalDate today = LocalDate.now();
        PriceHistory snap = PriceHistory.builder()
            .recordedOn(today)
            .openPrice(todayFirst.get()).highPrice(todayHigh.get())
            .lowPrice(todayLow.get()).closePrice(todayLast.get())
            .volume(todayVolume.get()).tradeCount(todayCount.get())
            .build();
        priceRepo.findByRecordedOn(today).ifPresentOrElse(e -> {
            e.setOpenPrice(snap.getOpenPrice()); e.setHighPrice(snap.getHighPrice());
            e.setLowPrice(snap.getLowPrice());   e.setClosePrice(snap.getClosePrice());
            e.setVolume(snap.getVolume());        e.setTradeCount(snap.getTradeCount());
            priceRepo.save(e);
        }, () -> priceRepo.save(snap));
        todayFirst.set(null); todayHigh.set(null); todayLow.set(null); todayLast.set(null);
        todayVolume.set(BigDecimal.ZERO); todayCount.set(0);
        log.info("Daily price snapshot flushed for {}", today);
    }

    @Transactional(readOnly = true)
    public List<PriceHistory> getHistory(int days) {
        return priceRepo.findByRecordedOnAfterOrderByRecordedOnAsc(LocalDate.now().minusDays(days));
    }
}
