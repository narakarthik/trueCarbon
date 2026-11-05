package com.truecarbon.trading.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.truecarbon.trading.domain.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    @Query("SELECT t FROM Trade t WHERE t.buyerId=:u OR t.sellerId=:u ORDER BY t.createdAt DESC")
    List<Trade> findByUser(@Param("u") UUID userId);
}
