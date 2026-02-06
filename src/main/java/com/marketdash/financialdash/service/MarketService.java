package com.marketdash.financialdash.service;

import com.marketdash.financialdash.client.UpbitClient;
import com.marketdash.financialdash.dto.MarketPriceHistoryResponse;
import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.dto.UpbitTickerResponse;
import com.marketdash.financialdash.entity.MarketPriceHistory;
import com.marketdash.financialdash.repository.MarketPriceHistoryRepository;
import com.marketdash.financialdash.service.cache.SimpleCacheStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class MarketService {

    private static final long CACHE_TTL = 3000; // 3초

    private final UpbitClient upbitClient;
    private final MarketPriceHistoryRepository historyRepository;
    private final SimpleCacheStore cacheStore = new SimpleCacheStore();

    public MarketService(
            UpbitClient upbitClient,
            MarketPriceHistoryRepository historyRepository
    ) {
        this.upbitClient = upbitClient;
        this.historyRepository = historyRepository;
    }

    /**
     * 업비트 실시간 시세 조회 + DB 저장 + 캐시
     */
    public MarketPriceResponse getUpbitPrice(String market) {
        String cacheKey = "UPBIT:" + market;

        MarketPriceResponse cached = cacheStore.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 1️⃣ 외부 API 호출
        UpbitTickerResponse ticker = upbitClient.getTicker(market);

        BigDecimal price = BigDecimal.valueOf(ticker.tradePrice());

        // 2️⃣ DB 저장
        historyRepository.save(
                MarketPriceHistory.of(
                        ticker.market(),
                        price,
                        "UPBIT"
                )
        );

        // 3️⃣ 응답 생성
        MarketPriceResponse response = new MarketPriceResponse(
                market.split("-")[1],
                ticker.market(),
                price,
                Instant.now()
        );

        // 4️⃣ 캐시 저장
        cacheStore.put(cacheKey, response, CACHE_TTL);

        return response;
    }

    public List<MarketPriceHistoryResponse> getUpbitPriceHistory(String market) {
        // 최신 300개를 가져온 다음(Desc), 차트용으로 Asc로 뒤집어서 반환
        List<MarketPriceHistory> rows = historyRepository.findTop300ByMarketOrderByCreatedAtDesc(market);
        java.util.Collections.reverse(rows);

        return rows.stream()
                .map(h -> new MarketPriceHistoryResponse(
                        h.getPrice(),
                        h.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
                ))
                .toList();
    }

    public void fetchAndSaveUpbitPrice(String market) {
        UpbitTickerResponse ticker = upbitClient.getTicker(market);

        MarketPriceHistory history = MarketPriceHistory.of(
                ticker.market(),                          // 예: KRW-BTC
                BigDecimal.valueOf(ticker.tradePrice()),  // 현재가
                "UPBIT"
        );

        historyRepository.save(history);
    }

}