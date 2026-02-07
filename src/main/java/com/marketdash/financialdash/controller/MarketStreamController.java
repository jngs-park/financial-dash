package com.marketdash.financialdash.controller;

import com.marketdash.financialdash.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class MarketStreamController {

    private final MarketService marketService;

    public MarketStreamController(MarketService marketService) {
        this.marketService = marketService;
    }

    // 예: /api/market/upbit/stream?market=KRW-BTC
    @GetMapping("/api/market/upbit/stream")
    public SseEmitter stream(@RequestParam String market) {
        return marketService.subscribe(market);
    }
}