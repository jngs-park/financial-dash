package com.marketdash.financialdash.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceResponse(
        String symbol,
        String market,
        BigDecimal price,
        Instant fetchedAt
) {}