package com.marketdash.financialdash.service;

import com.marketdash.financialdash.client.UpbitClient;
import com.marketdash.financialdash.dto.MarketPriceHistoryResponse;
import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.dto.MarketTickResponse;
import com.marketdash.financialdash.dto.UpbitTickerResponse;
import com.marketdash.financialdash.entity.MarketPriceHistory;
import com.marketdash.financialdash.repository.MarketPriceHistoryRepository;
import com.marketdash.financialdash.service.cache.LatestPriceStore;
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
    private final LatestPriceStore latestPriceStore;

    private final SimpleCacheStore cacheStore = new SimpleCacheStore();

    // market별 연결된 클라이언트 목록
    private final Map<String, CopyOnWriteArrayList<SseEmitter>> emittersByMarket = new ConcurrentHashMap<>();

    // ✅ emitter별 마지막 전송 tick key (중복 전송 방지)
    private final Map<SseEmitter, String> lastSentKeyByEmitter = new ConcurrentHashMap<>();

    public MarketService(UpbitClient upbitClient,
                         MarketPriceHistoryRepository historyRepository,
                         LatestPriceStore latestPriceStore) {
        this.upbitClient = upbitClient;
        this.historyRepository = historyRepository;
        this.latestPriceStore = latestPriceStore;
    }

    /* -------------------------
       1) REST 현재가 조회용 (캐시 사용)
       ------------------------- */
    public MarketPriceResponse getUpbitPrice(String market) {
        String cacheKey = "UPBIT:" + market;

        MarketPriceResponse cached = cacheStore.get(cacheKey);
        if (cached != null) return cached;

        MarketPriceResponse fresh = fetchFromUpbit(market);

        cacheStore.put(cacheKey, fresh, CACHE_TTL);
        return fresh;
    }

    /* -------------------------
       2) 스케줄러 전용: 항상 외부 호출 + DB저장 + Latest저장 + SSE
       ------------------------- */
    public void fetchAndSaveUpbitPrice(String market) {
        // ✅ 캐시 우회: 항상 외부 호출
        MarketPriceResponse response = fetchFromUpbit(market);

        // ✅ DB 히스토리 저장 (market은 "KRW-BTC" 그대로 저장해야 history 조회가 잘 됨)
        historyRepository.save(MarketPriceHistory.of(
                market,
                response.price(),
                "UPBIT"
        ));

        // ✅ latest 저장 (지금은 in-memory store지만 나중에 Redis로 바꾸기 쉬움)
        latestPriceStore.save(market, response);

        // ✅ SSE push
        publishTick(market, new MarketTickResponse(
                market,
                response.price(),
                response.fetchedAt()
        ));
    }

    private MarketPriceResponse fetchFromUpbit(String market) {
        UpbitTickerResponse ticker = upbitClient.getTicker(market);

        return new MarketPriceResponse(
                market.split("-")[1],
                ticker.market(),
                BigDecimal.valueOf(ticker.tradePrice()),
                Instant.now()
        );
    }

    /* -------------------------
       3) 히스토리 조회
       ------------------------- */
    public List<MarketPriceHistoryResponse> getUpbitPriceHistory(String market) {
        return historyRepository.findByMarketOrderByCreatedAtAsc(market)
                .stream()
                .map(h -> new MarketPriceHistoryResponse(
                        h.getPrice(),
                        h.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                ))
                .toList();
    }

    /* -------------------------
       4) SSE 구독
       ------------------------- */
    public SseEmitter subscribe(String market) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emittersByMarket
                .computeIfAbsent(market, k -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(market, emitter));
        emitter.onTimeout(() -> removeEmitter(market, emitter));
        emitter.onError(e -> removeEmitter(market, emitter));

        try {
            emitter.send(SseEmitter.event().name("ping").data("connected"));

            // ✅ 최신가 1회 push (페이지 처음 들어왔을 때 빈 그래프 방지)
            MarketPriceResponse latest = latestPriceStore.get(market);
            if (latest != null) {
                MarketTickResponse tick = new MarketTickResponse(
                        market,
                        latest.price(),
                        latest.fetchedAt()
                );

                // ✅ dedupe key 기록
                lastSentKeyByEmitter.put(emitter, buildTickKey(tick));

                emitter.send(SseEmitter.event().name("tick").data(tick));
            }

        } catch (Exception e) {
            removeEmitter(market, emitter);
        }

        return emitter;
    }

    public void publishTick(String market, MarketTickResponse tick) {
        var emitters = emittersByMarket.getOrDefault(market, new CopyOnWriteArrayList<>());
        String key = buildTickKey(tick);

        for (SseEmitter emitter : emitters) {
            try {
                String lastKey = lastSentKeyByEmitter.get(emitter);
                if (key.equals(lastKey)) {
                    // ✅ 같은 tick이면 중복 전송 스킵
                    continue;
                }

                emitter.send(SseEmitter.event().name("tick").data(tick));
                lastSentKeyByEmitter.put(emitter, key);

            } catch (Exception e) {
                removeEmitter(market, emitter);
            }
        }
    }

    private String buildTickKey(MarketTickResponse tick) {
        // ✅ MarketTickResponse는 fetchedAt이므로 여기서도 fetchedAt() 사용
        return tick.market()
                + "|" + tick.fetchedAt().toEpochMilli()
                + "|" + tick.price().toPlainString();
    }

    private void removeEmitter(String market, SseEmitter emitter) {
        var list = emittersByMarket.get(market);
        if (list != null) list.remove(emitter);
        lastSentKeyByEmitter.remove(emitter);
    }
}