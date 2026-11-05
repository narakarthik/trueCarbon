package com.truecarbon.trading.repository;

import com.truecarbon.trading.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o WHERE o.certificateId=:c AND o.side='SELL' AND o.status IN ('OPEN','PARTIALLY_FILLED') AND o.priceLimit<=:p ORDER BY o.priceLimit ASC, o.createdAt ASC")
    List<Order> findMatchingSellOrders(@Param("c") UUID certId, @Param("p") BigDecimal maxPrice);

    @Query("SELECT o FROM Order o WHERE o.certificateId=:c AND o.side='BUY' AND o.status IN ('OPEN','PARTIALLY_FILLED') AND o.priceLimit>=:p ORDER BY o.priceLimit DESC, o.createdAt ASC")
    List<Order> findMatchingBuyOrders(@Param("c") UUID certId, @Param("p") BigDecimal minPrice);

    @Query("SELECT o FROM Order o WHERE o.certificateId=:c AND o.side='SELL' AND o.status IN ('OPEN','PARTIALLY_FILLED') ORDER BY o.priceLimit ASC, o.createdAt ASC")
    List<Order> findCheapestSellOrders(@Param("c") UUID certId);

    @Query("SELECT o FROM Order o WHERE o.certificateId=:c AND o.side='BUY' AND o.status IN ('OPEN','PARTIALLY_FILLED') ORDER BY o.priceLimit DESC")
    List<Order> findOpenBuys(@Param("c") UUID certId);

    @Query("SELECT o FROM Order o WHERE o.certificateId=:c AND o.side='SELL' AND o.status IN ('OPEN','PARTIALLY_FILLED') ORDER BY o.priceLimit ASC")
    List<Order> findOpenSells(@Param("c") UUID certId);

    List<Order> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT o FROM Order o WHERE o.status IN ('OPEN','PARTIALLY_FILLED') AND o.expiresAt < CURRENT_TIMESTAMP")
    List<Order> findExpired();
}



