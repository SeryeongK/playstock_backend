# API 명세: 투자자 포트폴리오 (SCR-I007)

> 투자자가 자신의 투자 현황, 보유 조각, 수익 정보를 조회합니다.

---

## 데이터 모델

### UserProfile (사용자 프로필)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 사용자 ID |
| `email` | string | ✓ | 이메일 |
| `name` | string | ✓ | 사용자명 |
| `profileImageUrl` | string | ○ | 프로필 이미지 URL |
| `createdAt` | Date | ✓ | 가입 날짜 |

### Portfolio (포트폴리오 요약)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `userId` | UUID | ✓ | 사용자 ID |
| `totalAssets` | number | ✓ | 총 자산 (보유 조각 총 가치) |
| `totalDividends` | number | ✓ | 누적 배당금 |
| `totalHoldings` | number | ✓ | 총 보유 조각 수 |
| `totalChannels` | number | ✓ | 투자 중인 채널 수 |

### Holding (보유 자산)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 보유 기록 ID |
| `userId` | UUID | ✓ | 사용자 ID |
| `channelId` | UUID | ✓ | 채널 ID |
| `channelName` | string | ✓ | 채널명 |
| `channelThumbnail` | string | ○ | 채널 썸네일 |
| `quantity` | number | ✓ | 보유 조각 수 |
| `purchasePrice` | number | ✓ | 구매 당시 총 가격 |
| `currentValue` | number | ✓ | 현재 가치 |
| `currentPrice` | number | ✓ | 현재 조각 단가 |
| `gainLoss` | number | ✓ | 수익/손실 = currentValue - purchasePrice |
| `gainLossPercent` | number | ✓ | 수익률 (%) = (gainLoss / purchasePrice) × 100 |
| `accumulatedDividends` | number | ✓ | 누적 배당금 |
| `purchasedAt` | Date | ✓ | 구매 날짜 |

### Dividend (배당금 기록)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `date` | Date | ✓ | 배당금 지급 날짜 |
| `amount` | number | ✓ | 배당금 금액 |

---

## API 엔드포인트

### 포트폴리오 대시보드 조회
```http
GET /api/investor/portfolio
```

**인증 필요**: ✓

**응답 (200)**:
```json
{
  "data": {
    "profile": {
      "id": "user_uuid",
      "email": "user@example.com",
      "name": "김지은",
      "profileImageUrl": "https://...",
      "createdAt": "2025-01-01T00:00:00Z"
    },
    "summary": {
      "totalAssets": 12840,
      "totalDividends": 38750,
      "totalHoldings": 3,
      "totalChannels": 6
    },
    "holdings": [
      {
        "id": "holding_uuid",
        "channelId": "channel_uuid",
        "channelName": "뜬뜬 DdeunDdeun",
        "channelThumbnail": "https://...",
        "quantity": 10,
        "purchasePrice": 25000,
        "currentValue": 27500,
        "gainLoss": 2500,
        "gainLossPercent": 10,
        "accumulatedDividends": 1500,
        "purchasedAt": "2026-03-15T00:00:00Z"
      },
      {
        "id": "holding_uuid",
        "channelId": "channel_uuid",
        "channelName": "다른 채널",
        "quantity": 5,
        "purchasePrice": 10000,
        "currentValue": 10500,
        "gainLoss": 500,
        "gainLossPercent": 5,
        "accumulatedDividends": 800,
        "purchasedAt": "2026-04-01T00:00:00Z"
      }
    ]
  }
}
```

---

### 사용자 프로필 조회
```http
GET /api/investor/profile
```

**인증 필요**: ✓

**응답 (200)**:
```json
{
  "data": {
    "id": "user_uuid",
    "email": "user@example.com",
    "name": "김지은",
    "profileImageUrl": "https://...",
    "createdAt": "2025-01-01T00:00:00Z"
  }
}
```

---

### 보유 자산 상세 조회
```http
GET /api/investor/holdings/:holdingId
```

**인증 필요**: ✓

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `holdingId` | UUID | 보유 기록 ID |

**응답 (200)**:
```json
{
  "data": {
    "id": "holding_uuid",
    "channelId": "channel_uuid",
    "channelName": "뜬뜬 DdeunDdeun",
    "quantity": 10,
    "purchasePrice": 2500,
    "currentPrice": 2750,
    "currentValue": 27500,
    "gainLoss": 2500,
    "gainLossPercent": 10,
    "dividends": [
      {
        "date": "2026-04-01T00:00:00Z",
        "amount": 500
      },
      {
        "date": "2026-05-01T00:00:00Z",
        "amount": 500
      }
    ],
    "accumulatedDividends": 1500,
    "purchasedAt": "2026-03-15T00:00:00Z"
  }
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 401 | 미인증 |
| 403 | 다른 사용자의 자산 조회 |
| 404 | 보유 기록 없음 |

---

## 비즈니스 로직

### 포트폴리오 요약 계산
- **총 자산** = 모든 보유 자산의 현재 가치 합계
- **총 배당금** = 모든 보유 자산의 누적 배당금 합계
- **총 보유 조각 수** = 모든 채널의 보유 조각 수 합계
- **투자 중인 채널 수** = 보유 자산이 있는 채널의 개수

### 수익/손실 계산
- **gainLoss** = currentValue - purchasePrice
- **gainLossPercent** = (gainLoss / purchasePrice) × 100

### 배당금 지급
- 배당금은 채널의 배당 정책에 따라 정기(월간, 분기별 등)로 지급
- 누적 배당금에 누적됨

### 보유 자산 정렬
- UI에서는 최신 구매순으로 정렬되어 표시 (또는 수익률 높은 순)

---

## 유효성 검사

| 필드 | 규칙 |
|------|------|
| `holdingId` | UUID 형식 |

---

## 미결 사항

- 보유 기간 최소값 및 판매 정책
- 현재 가격 데이터 소스 및 갱신 빈도
- 배당금 정책 상세 정의
- 세금 계산 및 표시 필요 여부
