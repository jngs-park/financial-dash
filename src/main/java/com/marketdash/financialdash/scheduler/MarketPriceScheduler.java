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

    @Scheduled(fixedDelay = 3000) // 3초
    public void fetch() {
        marketService.fetchAndSaveUpbitPrice("KRW-BTC");
    }
}