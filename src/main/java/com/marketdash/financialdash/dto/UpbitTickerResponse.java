package com.marketdash.financialdash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpbitTickerResponse(
        String market,
        @JsonProperty("trade_price") Double tradePrice,
        @JsonProperty("trade_timestamp") Long tradeTimestamp,
        @JsonProperty("timestamp") Long timestamp
) {}