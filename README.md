# 📊 Financial Dash

> **실시간 금융 시세 데이터를 수집·가공·캐싱하여 제공하는  
Spring Boot 기반 금융 시장 대시보드 백엔드 프로젝트**

---

## 🔎 프로젝트 개요

**Financial Dash**는 외부 금융 API로부터 시세 데이터를 조회하고,  
이를 서비스 관점에 맞게 가공하여 REST API 형태로 제공하는  
**금융 데이터 백엔드 시스템**입니다.

단순 조회를 넘어서,
- 외부 API 의존성 최소화
- 응답 포맷 통일
- 캐시를 통한 성능 개선

을 목표로 설계되었습니다.

---

## 🧱 기술 스택

- **Language**: Java 17 (Amazon Corretto)
- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Web**: Spring Web (MVC)
- **HTTP Client**: RestTemplate
- **Caching**: In-memory cache (TTL 기반)
- **Version Control**: Git / GitHub

> ※ Redis, MySQL, Kafka는 이후 단계에서 확장 예정

---

## 🗂️ 프로젝트 구조
com.marketdash.financialdash
├─ controller     # API 엔드포인트
├─ service        # 비즈니스 로직
│   └─ cache      # 캐시 구현
├─ client         # 외부 API 연동
├─ dto            # 요청/응답 DTO
└─ FinancialDashApplication
### 설계 원칙
- Controller / Service / Client 책임 분리
- 외부 API DTO와 내부 서비스 DTO 분리
- 캐시 로직은 Service 계층에서 관리

---

## 🔗 주요 기능

### 1️⃣ Upbit 시세 조회 API
GET /api/market/upbit/ticker?market=KRW-BTC
**응답 예시**
```json
{
  "symbol": "BTC",
  "market": "KRW-BTC",
  "price": 103800000,
  "fetchedAt": "2026-02-05T06:25:50.704Z"
}
2️⃣ 외부 API 응답 가공
	•	Upbit 원본 응답을 그대로 노출하지 않음
	•	서비스 전용 응답 DTO(MarketPriceResponse)로 변환
	•	Double → BigDecimal 변환으로 지수 표기 최소화

⸻

3️⃣ 인메모리 캐시 적용
	•	캐시 키: UPBIT:{market}
	•	TTL: 3초
	•	구현: ConcurrentHashMap 기반
	•	목적: 외부 API 호출 횟수 감소 및 응답 속도 개선
Controller
 → Service (Cache 확인)
   → External API Client

🧠 설계 포인트
	•	외부 API 의존성 분리
	•	UpbitTickerResponse는 client 계층 전용
	•	컨트롤러는 내부 DTO만 반환
	•	확장 가능 구조
	•	인메모리 캐시 → Redis 교체 용이
	•	단일 시세 조회 → 다중 종목, 히스토리 확장 가능

🚀 실행 방법
./mvnw spring-boot:run
또는 IntelliJ에서 FinancialDashApplication 실행

기본 포트:
http://localhost:8082

📌 향후 개선 계획
	•	Redis 기반 캐시로 전환
	•	MySQL을 활용한 시세 이력 저장
	•	Kafka 기반 시세 이벤트 처리
	•	Swagger(OpenAPI) 문서화
	•	간단한 프론트엔드 대시보드 연동

⸻

✍️ 회고

단순 CRUD를 넘어
**“금융 데이터 흐름을 이해하고 설계하는 백엔드”**를 목표로 한 프로젝트입니다.

⸻

📎 GitHub
	•	Repository: financial-dash
---

