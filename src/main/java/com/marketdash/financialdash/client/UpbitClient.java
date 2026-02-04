package com.marketdash.financialdash.client;

import com.marketdash.financialdash.dto.UpbitTickerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UpbitClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public UpbitTickerResponse getTicker(String market) {
        String url = "https://api.upbit.com/v1/ticker?markets=" + market;

        ResponseEntity<UpbitTickerResponse[]> response =
                restTemplate.getForEntity(url, UpbitTickerResponse[].class);

        UpbitTickerResponse[] body = response.getBody();
        if (body == null || body.length == 0) {
            throw new IllegalStateException("Upbit ticker response is empty");
        }
        return body[0];
    }
}