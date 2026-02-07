package com.marketdash.financialdash.scheduler;

import com.marketdash.financialdash.dto.MarketTickResponse;
import com.marketdash.financialdash.service.MarketService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MarketTickScheduler {

    private final MarketService marketService;

    public MarketTickScheduler(MarketService marketService) {
        this.marketService = marketService;
    }

    // 일단 BTC만(나중에 여러 종목으로 확장)
    private static final String MARKET = "KRW-BTC";

    @Scheduled(fixedDelay = 3000)
    public void tick() {
        var price = marketService.getUpbitPrice(MARKET); // DB 저장도 여기서 발생
        marketService.publishTick(MARKET, new MarketTickResponse(
                MARKET,
                price.price(),
                Instant.now()
        ));
    }
}