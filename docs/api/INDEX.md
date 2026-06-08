# API 정의서 (프론트엔드·백엔드 공유)

> Playstock 플랫폼 전체 API 엔드포인트 및 데이터 모델 문서입니다.
> 프론트엔드 개발자와 백엔드 개발자가 함께 참조하는 공식 계약서입니다.

---

## 문서 구조

```
docs/api/
├─ INDEX.md              ← 이 문서 (API 개요 및 목록)
├─ ENDPOINTS.md          ← 전체 엔드포인트 빠른 참조
├─ DATA-MODELS.md        ← 공유 데이터 모델 및 열거형
├─ ENUMS.md              ← 모든 열거형(Enum) 정의
└─ {SCENARIO}/           ← 기능별 상세 문서
   ├─ creator-oauth.md      (SCR-C001)
   ├─ creator-channel-info.md (SCR-C002)
   ├─ creator-submission.md (SCR-C003)
   ├─ investor-channels.md  (SCR-I001)
   ├─ investor-detail.md    (SCR-I002)
   ├─ investor-purchase.md  (SCR-I003)
   └─ investor-portfolio.md (SCR-I007)
```

---

## API 기본 규격

### 응답 형식

**성공 응답** (200, 201):
```json
{
  "data": {...},
  "error": null
}
```

**실패 응답** (4xx, 5xx):
```json
{
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "사용자 친화적 메시지"
  }
}
```

### 인증

- **방식**: Bearer Token (JWT)
- **위치**: `Authorization: Bearer {token}` 헤더
- **실패**: 401 Unauthorized

### 기본 URL

| 환경 | URL |
|------|-----|
| 로컬 | `http://localhost:8080` |
| 운영 | `env(NEXT_PUBLIC_API_URL)` |

---

## 기능별 API 맵

### 크리에이터 (Creator) 기능

| 기능 | 관련 화면 | API 문서 |
|------|---------|---------|
| YouTube OAuth 연결 | SCR-C001 채널 연결 | [creator-oauth.md](creator-oauth.md) |
| 채널 정보 조회 (기본정보, 가치평가, 지표) | SCR-C002 채널 정보 확인 | [creator-channel-info.md](creator-channel-info.md) |
| 채널 설정 및 데이터 재동기화 | SCR-C002 채널 정보 확인 (설정) | [creator-channel-info.md](creator-channel-info.md) |
| 심사 서류 제출 | SCR-C003 심사 서류 제출 | [creator-submission.md](creator-submission.md) |

### 투자자 (Investor) 기능

| 기능 | 관련 화면 | API 문서 |
|------|---------|---------|
| 채널 목록 조회 | SCR-I001 채널 리스트 카로셀 | [investor-channels.md](investor-channels.md) |
| 채널 상세 조회 | SCR-I002 채널 상세 | [investor-detail.md](investor-detail.md) |
| 조각 구매 | SCR-I003 거래 패널 / SCR-I005 구매 완료 | [investor-purchase.md](investor-purchase.md) |
| 투자자 포트폴리오 | SCR-I007 투자자 마이페이지 | [investor-portfolio.md](investor-portfolio.md) |

---

## 엔드포인트 분류

### OAuth
- `GET /api/oauth/youtube/connect` — YouTube OAuth 동의 화면 URL 요청
- `GET /api/oauth/youtube/callback` — OAuth 콜백 처리

### 크리에이터 채널 관리
- `GET /api/creator/channels/{channelId}` — 채널 기본 정보 조회
- `GET /api/creator/channels/{channelId}/valuation` — 채널 가치평가 조회
- `GET /api/creator/channels/{channelId}/metrics` — 채널 세부 지표 조회
- `GET /api/creator/channels/{channelId}/settings` — 채널 설정 조회
- `PATCH /api/creator/channels/{channelId}/settings` — 채널 설정 변경
- `POST /api/creator/channels/{channelId}/sync` — 채널 데이터 재동기화

### 크리에이터 심사 (문서, 서류 제출)
- `GET /api/creator/channels/me/document-requirements` — 필요 서류 목록 조회
- `POST /api/creator/channels/me/documents` — 서류 업로드
- `DELETE /api/creator/channels/me/documents/{documentId}` — 서류 삭제
- `POST /api/creator/channels/me/applications` — 심사 신청 제출

### 투자자 채널 조회
- `GET /api/channels` — 채널 목록 조회 (필터링, 페이지네이션)
- `GET /api/channels/:id` — 채널 상세 조회
- `POST /api/channels/:id/like` — 채널 찜하기

### 투자자 구매
- `GET /api/purchases/preview/:channelId` — 구매 정보 미리보기
- `POST /api/purchases/calculate` — 가격 계산
- `POST /api/purchases` — 구매 주문 생성
- `POST /api/purchases/:id/complete` — 구매 완료

### 투자자 포트폴리오
- `GET /api/investor/portfolio` — 포트폴리오 대시보드 조회
- `GET /api/investor/profile` — 사용자 프로필 조회
- `GET /api/investor/holdings/:holdingId` — 보유 자산 상세 조회

---

## 공유 개념

### 채널 상태 (ChannelStatus)
```
PENDING   → 심사 중
ACTIVE    → 정상 판매 중
SOLD_OUT  → 완판
EXPIRED   → 만기 종료
SUSPENDED → 정지
```

### 채널 등급 (Tier)
```
BRONZE → #CD7F32
SILVER → #C0C0C0
GOLD   → #FFD700
```

### 도메인 용어

| 용어 | 의미 |
|------|------|
| Channel | 투자 대상 유튜브 채널 |
| Share / Fragment | 조각 (최소 거래 단위) |
| Holding | 보유 조각 |
| Order / Purchase | 구매 기록 |
| Dividend | 월 배당 |
| Creator | 채널 판매자 |
| Investor | 조각 구매자 |

---

## 사용 방법

1. **API 전체 목록** → [ENDPOINTS.md](ENDPOINTS.md) 참조
2. **데이터 모델 정의** → [DATA-MODELS.md](DATA-MODELS.md) 참조
3. **열거형(Enum) 값** → [ENUMS.md](ENUMS.md) 참조
4. **기능별 상세 설명** → 각 시나리오 문서 참조

---

## 개발 가이드

### 프론트엔드
- 각 기능 구현 시 해당 `{SCENARIO}.md` 문서 참조
- 모든 API 응답은 Zod 스키마로 검증 (`lib/api/`)
- 에러 코드와 메시지는 일관된 방식으로 처리

### 백엔드
- 각 엔드포인트 구현 시 해당 문서의 **API 엔드포인트** 섹션 준수
- 응답 필드명, 타입, 필수 여부는 정확히 지킬 것
- 신규 기능 추가 시 이 문서와 함께 갱신

### 설계 변경
- API 스키마 변경 시 관련 문서 갱신 필수
- Breaking Change는 백엔드/프론트엔드 리드와 함께 검토
- 버전 관리는 `/docs/api/history/` 폴더에 기록

---

## 관련 문서

- **백엔드 기능 명세**: `docs/backend/features/SCR-*.md`
- **프론트엔드 화면 명세**: `docs/frontend/spec/SCR-*.md`
- **진행 현황**: `docs/PROGRESS.md`
