package com.marketdash.financialdash.repository;

import com.marketdash.financialdash.entity.MarketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarketPriceHistoryRepository extends JpaRepository<MarketPriceHistory, Long> {
    List<MarketPriceHistory> findByMarketOrderByCreatedAtAsc(String market);
}