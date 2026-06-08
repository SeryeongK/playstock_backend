# 핀넥트 (Finnect) — 유튜브 채널 광고 수익 조각 투자 플랫폼

## 프로젝트 한 줄 소개
일반 투자자가 유튜브 채널의 광고 수익을 조각 단위로 투자하고, 월 배당을 받는 플랫폼.

## 컨텍스트 (중요)
- **개발자 배경:** PHP 풀스택 → Java/Spring 기초 있음, 깊이는 학습 중.
- **개발 기간:** 2025-05-11 ~ 2025-05-25 (15일)
- **목표:** 플로우 완성 우선. 시간 남으면 사기 탐지 깊이.
- **참가:** 핀테크 해커톤 (핀넥트 2026)

## 도메인 용어 (절대 바꾸지 말 것)
- **Channel** — 거래 대상 유튜브 채널
- **Share** — 채널의 조각 (주식 X, "조각" 통일)
- **Holding** — 사용자가 보유한 채널별 조각
- **Order** — 구매 (쇼핑몰 모델, 매칭 X)
- **Dividend** — 월 배당
- **FraudReport** — 사기 탐지 리포트
- **Reservation** — 결제 선점 (구조만 잡고 락은 나중)
- **Creator** — 채널을 등록하고 가격/수량 설정하는 유저 (판매자)
- **Investor** — 조각을 구매하는 유저 (구매자)

## 비즈니스 모델 (중요)
**수익 분배형 증권 (Revenue Share Note) 모델**:
- 크리에이터가 일정 기간 동안 광고 수익의 N%를 배당하기로 약정
- 투자자는 그 권리를 구매 → 만기까지 보유, 매월 배당 수령
- 만기 도래 시 권리 자동 소멸 (배당 종료)
- 영구 소유권 X (주식 아님), 원금 보장 X (채권 아님)
- **유사 모델:** 뮤직카우(음악 저작권), Royalty Exchange

**MVP 단계 거래 구조 (쇼핑몰):**
- 크리에이터가 가격/총 발행량/기간/배당율 설정 → 등록
- 투자자는 정찰제로 구매만 가능 (가격 변동 X)
- 사용자 간 매칭 거래 없음 (자본시장법 리스크 회피)
- V2 확장 시 2차 거래는 orders 테이블 확장으로 처리 예정

**법적 포지셔닝:**
- MVP는 가상 포인트로 자본시장법 우회
- 실서비스 시 조각투자 가이드라인 (2023.4 발표) 준수 검토 필요

## 유저 타입
- 한 사람이 Creator + Investor 동시 가능 (user_type을 한 컬럼이 아닌 권한 플래그로)

## 기술 스택
### Backend
- Spring Boot 3.4 / Java 21
- Gradle (Kotlin DSL)
- Spring Data JPA
- Spring AI 1.0 + Anthropic Claude
- Spring Scheduler
- SpringDoc OpenAPI (Swagger)
- PostgreSQL (개발도 운영도 동일, H2 금지)

### Frontend (별도 레포)
- Next.js 15 (App Router)
- shadcn/ui + Tailwind
- TanStack Query
- Zod

### External
- YouTube Data API v3
- Claude API (Spring AI 통해)

### 배포
- Backend: Render (무료 티어, Docker) + UptimeRobot (슬립 방지)
- Frontend: Vercel
- DB: Render PostgreSQL (무료)
- JVM 옵션 필수: `-Xmx400m -Xms200m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC`

## 패키지 구조
```
com.finnect
├─ user           # 회원, 인증
├─ channel        # 채널 등록, 조회
├─ valuation      # AI 가치평가
├─ fraud          # 사기 탐지 (L1 룰 + L2 LLM)
├─ trading        # 구매, 보유, 결제 선점 (쇼핑몰 모델)
├─ dividend       # 월 배당
├─ notification   # 알림
├─ common         # 공통 (예외, DTO, 유틸)
└─ infra          # YouTube/Claude 클라이언트
```

## 네이밍 규칙
- Controller: `~Controller` (REST 엔드포인트만)
- Service: `~Service` (유스케이스 단위)
- Repository: `~Repository` (JPA만)
- Entity: 도메인명 그대로 (`Channel`, `User`)
- DTO: `~Request`, `~Response` (절대 `~DTO` 금지)
- Domain event: `~Event` (예: `RiskLevelChangedEvent`)

## 코딩 규칙
### 절대 금지
- Lombok `@Data` 금지 (`@Getter`만 허용. `@Setter`는 정말 필요할 때만)
- Service에서 다른 Service 직접 호출 금지 → `ApplicationEventPublisher` 사용
- Controller에서 Entity 반환 금지 → 반드시 `~Response` DTO
- `application.yml`에 시크릿 하드코딩 금지 → 환경변수
- `H2` 사용 금지 (개발도 PostgreSQL)

### 권장
- 생성자 주입 (`@RequiredArgsConstructor` + `final`)
- `Optional` 사용 (null 반환 지양)
- Repository 메서드는 쿼리 메서드 우선, 복잡하면 `@Query`
- DTO 변환은 정적 팩토리 메서드 (`Response.from(entity)`)
- N+1 가능성 있는 코드엔 주석으로 명시

## API 응답 표준
```json
// 성공
{ "data": { ... }, "error": null }

// 실패
{ "data": null, "error": { "code": "INSUFFICIENT_SHARES", "message": "잔여 조각이 부족합니다" } }
```

## 테스트 정책
- **필수 TDD:** 사기 탐지 룰 엔진 (`fraud` 패키지)
- **권장 테스트:** 거래 매칭, 배당 계산, AI 호출 Mock
- **선택 테스트:** Controller 통합 테스트 (시간 남을 때)
- 테스트 코드 비율이 이력서에서 평가되므로 핵심 도메인은 반드시 작성

## 비즈니스 정책 (로직 결정 사항)

### 배당 정책
- **기준일:** 매월 1일 00:00 (KST) 시점의 holdings 기준
- **배당 대상:** status=ACTIVE & 만기 안 지난 채널만
- **계산:** total_amount = 추정 월수익 × dividend_rate, per_share = total_amount / total_shares
- **만기 도래 시:** 해당 월부터 dividend 생성 안 함

### 만기 처리
- **트리거:** 매일 자정 스케줄러
- **조건:** rights_end_at < NOW() AND status = ACTIVE
- **액션:** status=EXPIRED, 보유자에게 만기 알림, 이후 배당 계산 제외
- **holdings 보존:** 만기 후에도 holdings 행 유지, UI에서 "만기 종료" 표시

### 활동성 모니터링 (채널 비활성)
- **트리거:** 매일 자정 스케줄러, channel_metrics.last_upload_at 기준
- **30일 비활성:** warning_level=30, 크리에이터에게 알림
- **60일 비활성:** warning_level=60, 보유자 전체에게 경고 알림
- **90일 비활성:** warning_level=90, **status=SUSPENDED, 배당 중단**
- **복귀:** 다시 업로드 시 warning_level=NULL, status=ACTIVE (수동 또는 자동 검토)

### 환불 정책 (orders.status=REFUNDED)
- **자동 환불 케이스:**
  - share_reservation 만료 (결제 미완료) → 자동 잔여 반환
- **수동 환불 케이스 (운영 정책):**
  - 등록 후 사기 채널 판정 시 (fraud_reports HIGH)
  - 시스템 오류로 인한 중복 결제
- **환불 시:** users.point_balance 복원, channels.sold_shares 차감, holdings.shares 차감

### 사기 탐지 결과 반영
- **등록 시:** HIGH → status=SUSPENDED, 등록 차단
- **주간 재검사 시:**
  - HIGH 진입: 보유자에게 RISK_CHANGED 알림, 배당 계산 보류 (운영 판단)
  - LOW로 복귀: 자동 ACTIVE 복귀

## ERD (테이블 설계)

```
users
├─ id (BIGSERIAL PK)
├─ email (VARCHAR UNIQUE)
├─ nickname (VARCHAR)
├─ password_hash (VARCHAR)
├─ role (VARCHAR) — INVESTOR / CREATOR / ADMIN
├─ point_balance (BIGINT)
└─ created_at (TIMESTAMP)

channels
├─ id (BIGSERIAL PK)
├─ youtube_channel_id (VARCHAR UNIQUE)
├─ creator_id (FK → users)
├─ name (VARCHAR)
├─ category (VARCHAR) — FINANCE / TECH / LIFESTYLE / ENTERTAINMENT
├─ thumbnail_url (TEXT)
├─ status (VARCHAR) — PENDING / ACTIVE / SOLD_OUT / EXPIRED / SUSPENDED
├─ tier (VARCHAR) — BRONZE / SILVER / GOLD
├─ total_shares (INT)                   -- 크리에이터가 설정한 총 발행량
├─ sold_shares (INT)                    -- 판매 완료
├─ reserved_shares (INT)                -- 선점 중 (결제 진행)
├─ price (INT)                          -- 크리에이터가 설정한 단가 (정찰제)
├─ duration_months (INT)                -- 권리 기간 (12 / 24 / 36 등)
├─ dividend_rate (DECIMAL)              -- 광고수익 중 배당 비율 (0.20 = 20%)
├─ rights_start_at (TIMESTAMP)          -- 권리 시작일 (등록 승인 시점)
├─ rights_end_at (TIMESTAMP)            -- 만기일 (start + duration_months)
├─ warning_level (INT) — NULL(정상) / 30 / 60 / 90 (비활성 일수)
├─ warning_triggered_at (TIMESTAMP)
└─ created_at (TIMESTAMP)

channel_metrics (시계열 누적, 주1회)
├─ id (BIGSERIAL PK)
├─ channel_id (FK → channels)
├─ subscriber_count (BIGINT)
├─ avg_view_count (BIGINT)
├─ avg_likes (BIGINT)
├─ avg_comments (BIGINT)
├─ upload_count_30d (INT)
├─ last_upload_at (TIMESTAMP)
└─ snapshot_at (TIMESTAMP)

channel_valuations (재평가 시 새 row 추가)
├─ id (BIGSERIAL PK)
├─ channel_id (FK → channels)
├─ [AI 가치평가]
├─ score (INT 0~100)
├─ tier (VARCHAR) — BRONZE / SILVER / GOLD
├─ estimated_revenue (BIGINT)
├─ channel_value (BIGINT)
├─ multiple (DECIMAL)
├─ ai_reasoning (JSONB)
├─ [L1 정량 지표 스냅샷]
├─ subscriber_count_at_eval (BIGINT)
├─ avg_view_count_at_eval (BIGINT)
├─ active_rate (DECIMAL)               -- Rule 1: 구독자 대비 조회수 비율
├─ engagement_rate (DECIMAL)           -- Rule 4: (좋아요+댓글)/조회수
├─ subscriber_growth_rate (DECIMAL)    -- Rule 2: 30일 구독자 증가율
├─ repetitive_comment_rate (DECIMAL)   -- Rule 2: 댓글 반복 비율
├─ sub4sub_detected (BOOLEAN)          -- Rule 3: Sub4Sub 키워드 감지
├─ [카테고리 상대 비교]
├─ active_rate_vs_category (DECIMAL)   -- 카테고리 평균 대비 활동률 %
├─ engagement_rate_vs_category (DECIMAL) -- 카테고리 평균 대비 참여율 %
└─ evaluated_at (TIMESTAMP)

fraud_reports (매주 새 row 추가, 이력 보존)
├─ id (BIGSERIAL PK)
├─ channel_id (FK → channels)
├─ risk_level (VARCHAR) — HIGH / MEDIUM / LOW
├─ l1_signals (JSONB)
├─ l2_analysis (JSONB)
├─ evidence (TEXT)
├─ recommendation (VARCHAR)
└─ detected_at (TIMESTAMP)

category_benchmarks (카테고리 평균, 일1회 재계산)
├─ category (VARCHAR PK)
├─ avg_active_rate (DECIMAL)
├─ avg_engagement_rate (DECIMAL)
├─ sample_count (INT)
└─ updated_at (TIMESTAMP)

share_reservations
├─ id (BIGSERIAL PK)
├─ user_id (FK → users)
├─ channel_id (FK → channels)
├─ quantity (INT)
├─ reserved_at (TIMESTAMP)
├─ expires_at (TIMESTAMP)
└─ status (VARCHAR) — ACTIVE / CONFIRMED / EXPIRED / CANCELLED

orders (쇼핑몰 모델 - 구매 기록)
├─ id (BIGSERIAL PK)
├─ user_id (FK → users)                 -- 구매자
├─ channel_id (FK → channels)
├─ reservation_id (FK → share_reservations, NULLABLE)
├─ quantity (INT)
├─ price (INT)                          -- 구매 시점 단가 (스냅샷)
├─ total_amount (BIGINT)                -- quantity × price
├─ status (VARCHAR) — PENDING / PAID / CANCELLED / REFUNDED
├─ paid_at (TIMESTAMP)
└─ created_at (TIMESTAMP)

holdings
├─ id (BIGSERIAL PK)
├─ user_id (FK → users)
├─ channel_id (FK → channels)
├─ shares (INT)
├─ avg_price (INT)
├─ updated_at (TIMESTAMP)
└─ UNIQUE (user_id, channel_id)

dividends
├─ id (BIGSERIAL PK)
├─ channel_id (FK → channels)
├─ period (VARCHAR) — YYYYMM
├─ total_amount (BIGINT)
├─ per_share (BIGINT)
├─ status (VARCHAR) — PENDING / PAID / FAILED
└─ paid_at (TIMESTAMP)

dividend_payouts
├─ id (BIGSERIAL PK)
├─ dividend_id (FK → dividends)
├─ user_id (FK → users)
├─ shares_at_record (INT)
├─ amount (BIGINT)
└─ paid_at (TIMESTAMP)

notifications
├─ id (BIGSERIAL PK)
├─ user_id (FK → users)
├─ type (VARCHAR) — DIVIDEND_PAID / RISK_CHANGED / INACTIVE_WARNING
├─ payload (JSONB)
├─ read_at (TIMESTAMP)
└─ created_at (TIMESTAMP)
```

## DB 마이그레이션 정책

- **도구:** Flyway
- **위치:** `src/main/resources/db/migration/V{N}__{설명}.sql`
- **규칙:**
  - 한 번 적용된 파일 절대 수정 금지 → 새 파일로 추가
  - 로컬: `ddl-auto: create-drop` (재시작 시 깔끔)
  - 운영(Render): `ddl-auto: validate` + Flyway 자동 실행
- **의존성:** `flyway-core` + `flyway-database-postgresql`
- **90일 DB 교체 시:** 새 DB + 환경변수만 교체 → Flyway가 스키마 자동 재적용

## 동시성 정책 (MVP)
- **결정:** TBD-005에 따라 MVP에서는 동시성 처리 안 함
- **단, 구조는 미리 잡음:** Reservation 엔티티 + reserve/confirm 2단계 API
- **나중에 추가할 것:** `@Lock(PESSIMISTIC_WRITE)`, 만료 스케줄러

## 사기 탐지 정책
### L1 룰 (정량) — YouTube 공식 정책 기반
공신력 있는 룰만 사용. 임계값은 자체 설정임을 명시.

- **Rule 1: 비활성 구독자 비율** (Fake Engagement Policy 근거)
- **Rule 2: 반복 댓글 비율** (Spam Policy 근거)
- **Rule 3: Sub4Sub 키워드** (Spam Policy 근거)

### L2 LLM (정성) — Claude API
- **Rule 4: 미끼성 메타데이터** — 영상 제목/설명 vs 실제 콘텐츠 일관성

### 임계값 전략
- 절대값 + 카테고리 평균 대비 상대값 병용
- MVP는 시드 채널 8~10개로 카테고리 평균 구축
- **HypeAuditor 등 제3자 자료 인용 금지** (공신력 토론 회피)

## Sub-agents (자동/수동 호출)

| 에이전트 | 자동 호출 트리거 | 명시 호출 |
|---|---|---|
| **learning-note-keeper** | "X가 뭐야?", "X와 Y 차이는?", "X 왜 필요해?" | `/learn` |
| **decision-logger** | 옵션 비교 + 선택 토론, "왜 X로 결정?" | `/adr [주제]` |
| **feature-note-keeper** | "기능 시작/완료" 시그널 | `/feature [start\|update\|done\|reindex]` |
| **fraud-detection-expert** | `fraud` 패키지 작업, 룰 추가/수정 | (자동) |
| **test-writer** | 테스트 작성 작업 | `/test [대상]` |
| **pr-writer** | PR 본문 작성 요청 | `/pr [base브랜치]` |

### 자동 호출 원칙
- 학습성 질문 → `learning-note-keeper`로 위임
- 의사결정 토론 → `decision-logger`로 위임
- 사기 탐지 도메인 작업 → `fraud-detection-expert`로 위임
- 기능 시작/완료 시그널 → `feature-note-keeper`로 위임
- 동시에 여러 에이전트 필요하면 순차 호출 (한 번에 하나씩)

### 중복 방지 — 에이전트 역할 명확화
- **feature-note-keeper는 통합자.** 다른 에이전트 결과물을 모아 인덱싱.
- 같은 내용 두 곳에 쓰지 말 것:
  - 의사결정 상세 → ADR (Feature Note는 링크만)
  - 학습 내용 → learning-notes (Feature Note는 링크만)
  - PR 상세 → PR 본문 (Feature Note는 번호+요약만)

## 문서 구조

```
docs/
├─ features/              # 기능별 통합 문서 (1개 md = 1 기능)
│  ├─ INDEX.md             # 전체 진행 현황 대시보드
│  └─ F001-회원가입.md
├─ decisions/             # 기술 의사결정 (ADR)
│  ├─ INDEX.md
│  └─ ADR-001-...md
├─ learning-notes/        # 학습 기록 (카테고리별)
│  ├─ java.md
│  ├─ spring.md
│  └─ ...
└─ planning/
   └─ feature-spec.xlsx    # 기획서
```

**문서 작성 우선순위:**
1. **Feature Note**가 시작점 (코드 안 읽고 파악)
2. ADR / learning-note는 Feature Note에서 링크
3. PR은 코드 변경의 자세한 설명

## PR 작성 스타일

- **톤:** 반말 단문. "붙인다", "추가한다", "변경한다" — "합니다" 체 금지
- **구조:** 한 줄 배경 → 변경 → 테스트 체크리스트 → 참고(선택)
- **불필요한 것 제거:** 자명한 코드 설명 X, "의견 주세요" / "확인 부탁" 등 협업 표현 X (1인 레포)
- **테스트:** 재현 가능한 체크리스트 형식
- **참고:** 결정이 애매했던 부분만, 질문형 아닌 사실 서술형

예시:
```
PR #2에서 JWT 발급까지 됐고, 이번에 실제 검증 필터와 첫 인증 API를 붙인다.

## 변경
- `JwtAuthenticationFilter`: Bearer 토큰 파싱 → SecurityContext 등록
- `SecurityConfig`: 필터를 체인에 등록, CORS 환경변수화
- `GET /users/me`: 인증이 필요한 첫 번째 엔드포인트

## 테스트
- [ ] `POST /auth/login` 후 토큰으로 `GET /users/me` → 200
- [ ] 토큰 없이 `GET /users/me` → 403
- [ ] `./gradlew test` → 5/5 PASS

## 참고
`UserController`에서 Service 없이 Repository 직접 사용. 조회 로직이 한 줄이라 분리 보류.
```

## PR 코드 리뷰

CodeRabbit이 자동으로 PR마다 코드 리뷰 코멘트 작성.
설정: `.coderabbit.yaml` (한국어, 도메인 규칙 적용)
설치: https://github.com/marketplace/coderabbitai (public 레포 무료)

## 시연 시나리오
1. 회원가입 → 유저 타입 선택 (크리에이터 / 투자자)
2. 크리에이터: YouTube 채널 연결 → AI 심사 → 승인 → 조각 판매 등록
3. 투자자: 채널 리스트 → 채널 상세 (AI 평가 + 사기 탐지 결과 노출) → 매수 → 결제 완료
4. 매월 1일 배당 지급 (스케줄러)
5. **데모 차별화 포인트:** 사기 의심 채널 시연 데이터로 "심사 반려" 시연

## 참고 문서
- 기획서: `/docs/planning/feature-spec.xlsx`
- 학습 노트: `/docs/learning-notes/`
- 의사결정: `/docs/decisions/`
- API 문서: 로컬 `/swagger-ui.html`

## 작업 시 핵심 원칙
1. **한 번에 한 유스케이스만.** "거래 시스템 다 짜줘" 금지. "Order 엔티티만", "매수 API만" 같이 쪼개기.
2. **AI가 짠 코드 복붙 금지.** IDE에 직접 타이핑 (자바 손가락 익히기).
3. **모르는 어노테이션/패턴 나오면 즉시 질문 → learning-note에 자동 저장.**
4. **하루 끝에 5분, 그날 짠 코드 다시 읽기.**