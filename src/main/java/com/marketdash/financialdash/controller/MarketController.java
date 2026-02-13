package com.marketdash.financialdash.controller;

import com.marketdash.financialdash.dto.MarketPriceHistoryResponse;
import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.service.MarketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    // 현재가(캐시 적용)
    @GetMapping("/upbit/price")
    public MarketPriceResponse price(@RequestParam(defaultValue = "KRW-BTC") String market) {
        return marketService.getUpbitPrice(market);
    }

    // 히스토리(DB)
    @GetMapping("/upbit/history")
    public List<MarketPriceHistoryResponse> history(@RequestParam(defaultValue = "KRW-BTC") String market) {
        return marketService.getUpbitPriceHistory(market);
    }
}