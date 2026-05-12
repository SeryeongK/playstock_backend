# 핀넥트 (Finnect) — 유튜브 채널 광고 수익 조각 투자 플랫폼

## 프로젝트 한 줄 소개
일반 투자자가 유튜브 채널의 광고 수익을 조각 단위로 투자하고, 월 배당을 받는 플랫폼.

## 컨텍스트 (중요)
- **개발자 배경:** PHP 풀스택 → Java 풀스택 전환 중. Java/Spring 기초 있음, 깊이는 학습 중.
- **개발 기간:** 2025-05-11 ~ 2025-05-25 (15일)
- **목표:** 플로우 완성 우선. 시간 남으면 사기 탐지 깊이.
- **트랙:** 디지털 금융 (정보 격차 해소 / 사기 예방 / 금융 접근성)
- **참가:** 핀테크 해커톤 (핀넥트 2026)

## 도메인 용어 (절대 바꾸지 말 것)
- **Channel** — 거래 대상 유튜브 채널
- **Share** — 채널의 조각 (주식 X, "조각" 통일)
- **Holding** — 사용자가 보유한 채널별 조각
- **Order** — 매수 주문 (MVP는 1차 발행만)
- **Trade** — 체결된 거래
- **Dividend** — 월 배당
- **FraudReport** — 사기 탐지 리포트
- **Reservation** — 결제 선점 (구조만 잡고 락은 나중)
- **Creator** — 채널을 등록하는 유저 (크리에이터)
- **Investor** — 조각을 매수하는 유저 (투자자)

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
├─ trading        # 매수, 보유, 결제 선점
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

## PR 코드 리뷰

CodeRabbit이 자동으로 PR마다 코드 리뷰 코멘트 작성.
설정: `.coderabbit.yaml` (한국어, 도메인 규칙 적용)
설치: https://github.com/marketplace/coderabbitai (public 레포 무료)

## 시연 시나리오 (5/22 작업)
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
