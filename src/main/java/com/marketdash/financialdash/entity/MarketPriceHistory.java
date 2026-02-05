package com.marketdash.financialdash.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_price_history")
public class MarketPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String market;

    private BigDecimal price;

    private String source;

    private LocalDateTime createdAt;

    protected MarketPriceHistory() {}

    public static MarketPriceHistory of(String market, BigDecimal price, String source) {
        MarketPriceHistory h = new MarketPriceHistory();
        h.market = market;
        h.price = price;
        h.source = source;
        h.createdAt = LocalDateTime.now();
        return h;
    }

    // ✅ 여기부터 명시적 getter
    public Long getId() {
        return id;
    }

    public String getMarket() {
        return market;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getSource() {
        return source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}