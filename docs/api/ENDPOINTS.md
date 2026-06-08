# API 엔드포인트 빠른 참조

> 모든 엔드포인트 목록 (HTTP 메서드별 정렬)

---

## GET 요청

### OAuth
| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `GET /api/oauth/youtube/connect` | YouTube OAuth 동의 화면 URL 요청 | ✓ |
| `GET /api/oauth/youtube/callback` | OAuth 콜백 처리 | ✗ |

### 크리에이터 채널
| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `GET /api/creator/channels/{channelId}` | 채널 기본 정보 조회 | ✓ |
| `GET /api/creator/channels/{channelId}/valuation` | 채널 가치평가 조회 | ✓ |
| `GET /api/creator/channels/{channelId}/metrics` | 채널 세부 지표 조회 | ✓ |
| `GET /api/creator/channels/{channelId}/settings` | 채널 설정 조회 | ✓ |
| `GET /api/creator/channels/me/document-requirements` | 필요 서류 목록 조회 | ✓ |

### 투자자 채널
| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `GET /api/channels` | 채널 목록 조회 (쿼리: status, page, limit, popular) | ✓ |
| `GET /api/channels/:id` | 채널 상세 조회 | ✓ |

### 투자자 구매
| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `GET /api/purchases/preview/:channelId` | 구매 정보 미리보기 | ✓ |

### 투자자 포트폴리오
| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `GET /api/investor/portfolio` | 포트폴리오 대시보드 조회 | ✓ |
| `GET /api/investor/profile` | 사용자 프로필 조회 | ✓ |
| `GET /api/investor/holdings/:holdingId` | 보유 자산 상세 조회 | ✓ |

---

## POST 요청

| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `POST /api/creator/channels/me/documents` | 서류 파일 업로드 | ✓ |
| `POST /api/creator/channels/me/applications` | 심사 신청 제출 | ✓ |
| `POST /api/creator/channels/{channelId}/sync` | 채널 데이터 재동기화 | ✓ |
| `POST /api/channels/:id/like` | 채널 찜하기 (토글) | ✓ |
| `POST /api/purchases/calculate` | 구매 가격 계산 | ✓ |
| `POST /api/purchases` | 구매 주문 생성 | ✓ |
| `POST /api/purchases/:id/complete` | 구매 완료 처리 | ✓ |

---

## PATCH 요청

| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `PATCH /api/creator/channels/{channelId}/settings` | 채널 설정 변경 | ✓ |

---

## DELETE 요청

| 엔드포인트 | 설명 | 인증 |
|-----------|------|------|
| `DELETE /api/creator/channels/me/documents/{documentId}` | 서류 삭제 | ✓ |

---

## 응답 코드 정리

### 성공
- `200 OK` — 조회, 갱신 성공
- `201 Created` — 생성 성공
- `202 Accepted` — 비동기 작업 승인 (예: 데이터 재동기화)

### 클라이언트 에러
- `400 Bad Request` — 잘못된 요청 본문/파라미터
- `401 Unauthorized` — 미인증
- `403 Forbidden` — 권한 없음
- `404 Not Found` — 리소스 없음
- `409 Conflict` — 상태 충돌 (예: 중복, 판매 수량 부족)
- `422 Unprocessable Entity` — 유효성 검증 실패
- `429 Too Many Requests` — Rate Limit 초과

### 서버 에러
- `500 Internal Server Error` — 서버 내부 에러

---

## 자세한 명세 (기능별)

- [크리에이터 OAuth 연결](creator-oauth.md) (SCR-C001)
- [크리에이터 채널 정보](creator-channel-info.md) (SCR-C002)
- [크리에이터 심사 제출](creator-submission.md) (SCR-C003)
- [투자자 채널 목록](investor-channels.md) (SCR-I001)
- [투자자 채널 상세](investor-detail.md) (SCR-I002)
- [투자자 구매](investor-purchase.md) (SCR-I003)
- [투자자 포트폴리오](investor-portfolio.md) (SCR-I007)
