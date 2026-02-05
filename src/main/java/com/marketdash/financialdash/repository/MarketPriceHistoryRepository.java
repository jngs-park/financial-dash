package com.marketdash.financialdash.repository;

import com.marketdash.financialdash.entity.MarketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketPriceHistoryRepository
        extends JpaRepository<MarketPriceHistory, Long> {
}