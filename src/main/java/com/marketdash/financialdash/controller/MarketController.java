package com.marketdash.financialdash.controller;

import com.marketdash.financialdash.dto.MarketPriceResponse;
import com.marketdash.financialdash.service.MarketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;
import com.marketdash.financialdash.dto.MarketPriceHistoryResponse;

@RestController
@RequestMapping("/api/market")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/upbit/ticker")
    public MarketPriceResponse ticker(
            @RequestParam(defaultValue = "KRW-BTC") String market
    ) {
        // ✅ 서비스가 반환하는 타입 그대로 리턴
        return marketService.getUpbitPrice(market);
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/upbit/history")
    @ResponseBody
    public List<MarketPriceHistoryResponse> upbitHistory(@RequestParam String market) {
        return marketService.getUpbitPriceHistory(market);
    }
}