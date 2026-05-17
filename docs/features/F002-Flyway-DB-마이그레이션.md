---
feature: F002
title: Flyway DB 마이그레이션 설정
status: 🟢 완료
started: 2026-05-14
finished: 2026-05-14
---

# F002: Flyway DB 마이그레이션 설정

**상태:** 🟢 완료
**기간:** 2026-05-14 ~ 2026-05-14
**관련 화면:** 없음 (인프라/설정)
**관련 PR:** TBD
**관련 ADR:** -

---

## 1. 설계 (Why & What)

### 목적
`ddl-auto: create-drop` 방식 대신 Flyway로 스키마를 버전 관리한다. 로컬·운영 모두 동일한 PostgreSQL을 사용하고, Render 배포 시 DB를 새로 교체하더라도 Flyway가 스키마를 자동 재적용할 수 있게 한다.

### 입력 / 출력
- 입력: `src/main/resources/db/migration/V{N}__{설명}.sql` 파일
- 출력: 애플리케이션 기동 시 DB에 스키마 자동 적용, `flyway_schema_history` 테이블로 이력 관리

### 핵심 흐름
1. 앱 기동 → Flyway가 `flyway_schema_history` 테이블 확인
2. 미적용 마이그레이션 파일 감지 → 순서대로 실행
3. JPA `ddl-auto: validate`로 엔티티와 스키마 일치 검증

### 주요 결정
- `ddl-auto`는 `validate`로 고정 (로컬·운영 동일). 스키마 변경은 반드시 새 V 파일로.
- `baseline-on-migrate: false` — 기존 DB 없이 항상 V1부터 깔끔하게 시작.

---

## 2. 구현 (How)

### 패키지
인프라 설정 (패키지 코드 없음, `build.gradle.kts` + `application.yml` + SQL)

### 파일 목록
- `build.gradle.kts` — `flyway-core`, `flyway-database-postgresql` 의존성 추가
- `src/main/resources/application.yml` — `spring.flyway` 블록 추가, `ddl-auto: validate` 확정
- `src/main/resources/db/migration/V1__init_schema.sql` — ERD 전체 12개 테이블 + 11개 인덱스

### API
없음

### DB 변경

V1__init_schema.sql 적용 결과:

| 테이블 | 설명 |
|--------|------|
| `users` | 회원 (INVESTOR / CREATOR / ADMIN) |
| `channels` | 거래 대상 채널, 발행량·가격·배당율·만기 포함 |
| `channel_metrics` | 주간 지표 스냅샷 (구독자·조회수·좋아요 등) |
| `channel_valuations` | AI 가치평가 결과 이력 |
| `fraud_reports` | 사기 탐지 결과 이력 |
| `category_benchmarks` | 카테고리 평균 지표 (일1회 재계산) |
| `share_reservations` | 결제 선점 (ACTIVE / CONFIRMED / EXPIRED / CANCELLED) |
| `orders` | 구매 기록 (쇼핑몰 모델) |
| `holdings` | 보유 조각 (user_id + channel_id UNIQUE) |
| `dividends` | 월 배당 헤더 (channel + period UNIQUE) |
| `dividend_payouts` | 배당 개별 지급 내역 |
| `notifications` | 알림 (DIVIDEND_PAID / RISK_CHANGED / INACTIVE_WARNING) |

인덱스 11개: channels(creator_id, status), channel_metrics(channel_id), fraud_reports(channel_id), share_reservations(user_id, channel_id), orders(user_id, channel_id), holdings(user_id), dividends(channel_id), notifications(user_id)

### 의존성
- 선행 기능: F001 (Spring Boot 초기 셋업)
- 외부 API: 없음
- 라이브러리: `org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql`

### 핵심 코드 (스니펫)
```kotlin
// build.gradle.kts
implementation("org.flywaydb:flyway-core")
implementation("org.flywaydb:flyway-database-postgresql")
```

---

## 3. 검증 (Verify)

### 테스트
- 별도 테스트 없음 (인프라 설정). Gradle 빌드 시 의존성 해석 성공으로 확인.

### 수동 검증
- [ ] `./gradlew bootRun` 기동 시 Flyway 로그에 "Successfully applied 1 migration" 출력
- [ ] PostgreSQL에 `flyway_schema_history` 테이블 생성 및 V1 행 존재
- [ ] 12개 테이블 + 11개 인덱스 정상 생성
- [ ] 두 번째 기동 시 "Schema ... is up to date. No migration necessary" 출력

### Swagger
없음 (이 기능 자체는 엔드포인트 없음)

---

## 4. 후속 (Follow-up)

### 알려진 한계
- V1 파일은 한 번 적용되면 절대 수정 금지. 변경이 필요하면 반드시 V2, V3 파일을 새로 추가.
- 로컬에서 스키마를 리셋하려면 DB 드롭 후 재생성 필요 (`flyway_schema_history` 초기화).

### 미해결
- Spring Security + JWT 추가 후 `users` 테이블에 컬럼 추가 필요 시 V2 파일 작성 예정.

### 학습 포인트
- [Flyway 마이그레이션 파일 명명 규칙 및 동작 원리](../learning-notes/spring.md)

### 향후 개선
- V2 이후 파일: 회원가입(F003) 작업 시 `refresh_token` 컬럼 등 추가 시 작성
- Render 90일 DB 교체 시: 새 DB + 환경변수만 교체 → Flyway가 V1부터 자동 재적용
