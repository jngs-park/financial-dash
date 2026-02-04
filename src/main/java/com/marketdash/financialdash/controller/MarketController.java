package com.marketdash.financialdash.controller;

import com.marketdash.financialdash.dto.UpbitTickerResponse;
import com.marketdash.financialdash.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/upbit/ticker")
    public UpbitTickerResponse ticker(@RequestParam(defaultValue = "KRW-BTC") String market) {
        return marketService.getUpbitPrice(market);
    }
}