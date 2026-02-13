package com.marketdash.financialdash.service.cache;

import com.marketdash.financialdash.dto.MarketPriceResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LatestPriceStore {

    private final Map<String, MarketPriceResponse> latestPrices = new ConcurrentHashMap<>();

    public void save(String market, MarketPriceResponse response) {
        latestPrices.put(market, response);
    }

    public MarketPriceResponse get(String market) {
        return latestPrices.get(market);
    }
}