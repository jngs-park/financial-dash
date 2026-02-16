# Architecture

## High-level
- Scheduler(3s) → Upbit API 호출 → MySQL(history) 저장 → Redis(latest) 저장 → SSE(tick) Push
- Browser(index.html) → SSE 구독 → 최신 tick을 차트에 push → 최근 N분만 window로 표시 + MA 계산

## Data flow
1) `MarketPriceScheduler`가 `MarketService.fetchAndSaveUpbitPrice("KRW-BTC")` 호출
2) `MarketService`가 UpbitClient로 시세 조회
3) 결과를:
    - MySQL `market_price_history`에 저장
    - Redis(또는 LatestPriceStore) 에 최신가 저장
    - SSE로 `tick` 이벤트 전송
4) 브라우저는 `EventSource`로 tick 수신 → 차트 갱신

## Why SSE?
- 서버 → 클라이언트 단방향 스트리밍에 단순/안정적
- HTTP 기반이라 인프라 구성/디버깅이 비교적 쉬움
