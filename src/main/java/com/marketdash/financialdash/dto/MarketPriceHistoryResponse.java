package com.marketdash.financialdash.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketPriceHistoryResponse(
        BigDecimal price,
        Instant createdAt
) {}