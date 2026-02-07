package com.marketdash.financialdash.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketTickResponse(
        String market,
        BigDecimal price,
        Instant createdAt
) {}