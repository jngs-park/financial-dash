package com.marketdash.financialdash.service;

import com.marketdash.financialdash.client.UpbitClient;
import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.dto.UpbitTickerResponse;
import com.marketdash.financialdash.service.cache.SimpleCacheStore;
import org.springframework.stereotype.Service;
import com.marketdash.financialdash.entity.MarketPriceHistory;
import com.marketdash.financialdash.repository.MarketPriceHistoryRepository;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class MarketService {

    private static final long CACHE_TTL = 3000; // 3초

    private final UpbitClient upbitClient;
    private final SimpleCacheStore cacheStore = new SimpleCacheStore();
    private final MarketPriceHistoryRepository historyRepository;
    public MarketService(UpbitClient upbitClient,
                         MarketPriceHistoryRepository historyRepository) {
        this.upbitClient = upbitClient;
        this.historyRepository = historyRepository;
    }

    public MarketPriceResponse getUpbitPrice(String market) {
        String cacheKey = "UPBIT:" + market;

        // ✅ 캐시에서 꺼낼 때 타입은 MarketPriceResponse
        MarketPriceResponse cached = cacheStore.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 외부 API 호출
        UpbitTickerResponse ticker = upbitClient.getTicker(market);

        MarketPriceResponse response = new MarketPriceResponse(
                market.split("-")[1],
                ticker.market(),
                BigDecimal.valueOf(ticker.tradePrice()),
                Instant.now()
        );

        // ✅ 캐시에 넣는 것도 MarketPriceResponse
        cacheStore.put(cacheKey, response, CACHE_TTL);

        return response;
    }
}