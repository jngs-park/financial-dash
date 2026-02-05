package com.marketdash.financialdash.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "market_price_history")
public class MarketPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String market;

    private BigDecimal price;

    private String source;

    private Instant createdAt;

    protected MarketPriceHistory() {}

    public MarketPriceHistory(String market, BigDecimal price, String source) {
        this.market = market;
        this.price = price;
        this.source = source;
        this.createdAt = Instant.now();
    }
}