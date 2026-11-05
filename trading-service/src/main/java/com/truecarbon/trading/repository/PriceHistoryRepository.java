package com.truecarbon.trading.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.truecarbon.trading.domain.PriceHistory;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {
    Optional<PriceHistory> findByRecordedOn(LocalDate date);
    List<PriceHistory> findByRecordedOnAfterOrderByRecordedOnAsc(LocalDate from);
}