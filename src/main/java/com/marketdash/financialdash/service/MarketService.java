package com.marketdash.financialdash.service;

import com.marketdash.financialdash.client.UpbitClient;
import com.marketdash.financialdash.dto.UpbitTickerResponse;
import org.springframework.stereotype.Service;

@Service
public class MarketService {

    private final UpbitClient upbitClient;

    public MarketService(UpbitClient upbitClient) {
        this.upbitClient = upbitClient;
    }

    public UpbitTickerResponse getUpbitPrice(String market) {
        return upbitClient.getTicker(market);
    }
}