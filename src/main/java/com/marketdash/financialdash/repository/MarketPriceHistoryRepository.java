package com.marketdash.financialdash.repository;

import com.marketdash.financialdash.entity.MarketPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarketPriceHistoryRepository extends JpaRepository<MarketPriceHistory, Long> {

    // 최근 300개를 최신순으로 가져오기
    List<MarketPriceHistory> findTop300ByMarketOrderByCreatedAtDesc(String market);
}