package com.truecarbon.trading.config;

import com.truecarbon.trading.client.UserServiceClient;
import com.truecarbon.trading.domain.Order;
import com.truecarbon.trading.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryJob {

    private final OrderRepository   orderRepo;
    private final UserServiceClient userClient;

    @Scheduled(cron = "0 0 1 * * *") // runs at 1 AM daily
    @Transactional
    public void expireOrders() {
        var expired = orderRepo.findExpired();
        log.info("Expiring {} orders", expired.size());

        for (Order order : expired) {
            // Release reserved credits for SELL orders
            if ("SELL".equals(order.getSide())) {
                userClient.releaseCredits(
                    order.getUserId(),
                    order.getCertificateId(),
                    order.getRemainingCredits()
                );
            }
            order.setStatus("EXPIRED");
            orderRepo.save(order);
        }
    }
}
