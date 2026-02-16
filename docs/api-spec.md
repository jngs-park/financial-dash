# API & SSE Spec

## REST
### GET /api/market/upbit/history?market=KRW-BTC
Response: JSON array
```json
[
  { "price": 98080000, "createdAt": "2026-02-16T11:22:33Z" }
]
```

### GET /api/market/upbit/ticker?market=KRW-BTC
Response
```json
{ "symbol": "BTC", "market": "KRW-BTC", "price": 98080000, "fetchedAt": "2026-02-16T11:22:33Z" }
```

## SSE
### GET /api/market/upbit/stream?market=KRW-BTC
Events:
- `ping` : 연결 확인용
- `tick` : 가격 1건

tick payload (MarketTickResponse)
```json
{ "market": "KRW-BTC", "price": 98080000, "fetchedAt": "2026-02-16T11:22:33Z" }
```

Client notes:
- 중복 tick(같은 timestamp/price)이 들어오면 프론트에서 dedupe 가능
- 연결 직후 “최신 1건”을 먼저 보내면 초기 렌더가 자연스러움
