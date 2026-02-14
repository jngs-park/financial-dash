package com.marketdash.financialdash.service.cache;

import com.marketdash.financialdash.dto.MarketPriceResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class LatestPriceStore {

    private static final String KEY_PREFIX = "latest:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redis;

    public LatestPriceStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void save(String market, MarketPriceResponse response) {
        String key = KEY_PREFIX + market;

        // Hash에 문자열로 쪼개서 저장 (JSON 변환 없음)
        redis.opsForHash().put(key, "symbol", response.symbol());
        redis.opsForHash().put(key, "market", response.market());
        redis.opsForHash().put(key, "price", response.price().toPlainString());
        redis.opsForHash().put(key, "fetchedAt", String.valueOf(response.fetchedAt().toEpochMilli()));

        redis.expire(key, TTL);
    }

    public MarketPriceResponse get(String market) {
        String key = KEY_PREFIX + market;

        Map<Object, Object> map = redis.opsForHash().entries(key);
        if (map == null || map.isEmpty()) return null;

        try {
            String symbol = (String) map.get("symbol");
            String mkt = (String) map.get("market");
            String priceStr = (String) map.get("price");
            String fetchedAtStr = (String) map.get("fetchedAt");

            if (symbol == null || mkt == null || priceStr == null || fetchedAtStr == null) return null;

            return new MarketPriceResponse(
                    symbol,
                    mkt,
                    new java.math.BigDecimal(priceStr),
                    Instant.ofEpochMilli(Long.parseLong(fetchedAtStr))
            );
        } catch (Exception e) {
            return null;
        }
    }
}