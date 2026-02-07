package com.marketdash.financialdash.service;

import com.marketdash.financialdash.client.UpbitClient;
import com.marketdash.financialdash.dto.MarketPriceHistoryResponse;
import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.dto.MarketTickResponse;
import com.marketdash.financialdash.dto.UpbitTickerResponse;
import com.marketdash.financialdash.entity.MarketPriceHistory;
import com.marketdash.financialdash.repository.MarketPriceHistoryRepository;
import com.marketdash.financialdash.service.cache.SimpleCacheStore;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MarketService {

    private static final long CACHE_TTL = 3000; // 3초
    private static final long SSE_TIMEOUT = 60L * 60 * 1000; // 1시간

    private final UpbitClient upbitClient;
    private final MarketPriceHistoryRepository historyRepository;

    private final SimpleCacheStore cacheStore = new SimpleCacheStore();

    // market별 연결된 클라이언트 목록
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByMarket = new ConcurrentHashMap<>();

    public MarketService(UpbitClient upbitClient,
                         MarketPriceHistoryRepository historyRepository) {
        this.upbitClient = upbitClient;
        this.historyRepository = historyRepository;
    }

    /** 현재가 조회 + DB 저장 */
    public MarketPriceResponse getUpbitPrice(String market) {
        String cacheKey = "UPBIT:" + market;

        MarketPriceResponse cached = cacheStore.get(cacheKey);
        if (cached != null) return cached;

        UpbitTickerResponse ticker = upbitClient.getTicker(market);

        MarketPriceResponse response = new MarketPriceResponse(
                market.split("-")[1],
                ticker.market(),
                BigDecimal.valueOf(ticker.tradePrice()),
                Instant.now()
        );

        // 히스토리 저장
        historyRepository.save(MarketPriceHistory.of(
                response.market(),
                response.price(),
                "UPBIT"
        ));

        cacheStore.put(cacheKey, response, CACHE_TTL);
        return response;
    }

    /** 스케줄러가 호출: 현재가 조회 + 저장 + SSE 푸시 */
    public void fetchAndSaveUpbitPrice(String market) {
        MarketPriceResponse response = getUpbitPrice(market);

        publishTick(market, new MarketTickResponse(
                response.market(),     // ✅ record는 (market, price, createdAt)
                response.price(),
                response.fetchedAt()
        ));
    }

    /** 히스토리 조회 (Chart.js에서 사용) */
    public List<MarketPriceHistoryResponse> getUpbitPriceHistory(String market) {
        return historyRepository.findByMarketOrderByCreatedAtAsc(market)
                .stream()
                .map(h -> new MarketPriceHistoryResponse(
                        h.getPrice(),
                        h.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant() // ✅ LocalDateTime -> Instant
                ))
                .toList();
    }

    /** SSE 구독 */
    public SseEmitter subscribe(String market) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emittersByMarket.computeIfAbsent(market, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(market, emitter));
        emitter.onTimeout(() -> removeEmitter(market, emitter));
        emitter.onError(e -> removeEmitter(market, emitter));

        // 연결 확인용 ping
        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));
        } catch (Exception e) {
            removeEmitter(market, emitter);
        }
        return emitter;
    }

    /** SSE로 가격 푸시 */
    public void publishTick(String market, MarketTickResponse tick) {
        var emitters = emittersByMarket.getOrDefault(market, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("tick").data(tick));
            } catch (Exception e) {
                removeEmitter(market, emitter);
            }
        }
    }

    private void removeEmitter(String market, SseEmitter emitter) {
        var list = emittersByMarket.get(market);
        if (list != null) list.remove(emitter);
    }
}