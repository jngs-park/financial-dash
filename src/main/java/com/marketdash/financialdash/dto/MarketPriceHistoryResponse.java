package com.marketdash.financialdash.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketPriceHistoryResponse(
        BigDecimal price,
        LocalDateTime time
) {}