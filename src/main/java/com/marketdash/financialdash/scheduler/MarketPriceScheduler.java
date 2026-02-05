package com.marketdash.financialdash.scheduler;

import com.marketdash.financialdash.service.MarketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketPriceScheduler {

    private final MarketService marketService;

    public MarketPriceScheduler(MarketService marketService) {
        this.marketService = marketService;
    }

    @Scheduled(fixedRate = 5000) // 5초마다
    public void saveBtcPrice() {
        marketService.fetchAndSaveUpbitPrice("KRW-BTC");
    }
}