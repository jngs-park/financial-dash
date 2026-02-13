package com.marketdash.financialdash.controller;

import com.marketdash.financialdash.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class MarketSseController {

    private final MarketService marketService;

    public MarketSseController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/api/market/upbit/stream")
    public SseEmitter stream(@RequestParam String market) {
        return marketService.subscribe(market);
    }
}