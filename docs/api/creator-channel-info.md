# API 명세: 크리에이터 채널 정보 (SCR-C002)

> 크리에이터가 자신의 연동 채널 정보, 가치평가, 세부 지표를 조회하고 설정을 관리합니다.

---

## 데이터 모델

### CreatorChannel (크리에이터 채널 — 기본 정보)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | number | ✓ | 채널 기본 키 |
| `channelName` | string | ✓ | 채널명 |
| `channelHandle` | string | ✓ | 채널 핸들 (@channel_id) |
| `thumbnailUrl` | string | ○ | 채널 프로필 이미지 URL |
| `connectionStatus` | enum | ✓ | CONNECTED \| DISCONNECTED |
| `reviewStatus` | enum | ✓ | UNREGISTERED \| REGISTERED \| UNDER_REVIEW \| APPROVED \| REJECTED |
| `lastSyncedAt` | Date | ○ | 마지막 데이터 동기화 시각 |
| `subscriberCount` | number | ✓ | 구독자 수 |
| `videoCount` | number | ✓ | 영상 수 |
| `totalViewCount` | number | ✓ | 총 조회수 |
| `category` | string | ✓ | 채널 카테고리 |
| `joinedAt` | Date | ✓ | YouTube 채널 개설일 |
| `avgMonthlyViews` | number | ✓ | 6개월 평균 월간 조회수 |
| `avgViewDuration` | string | ✓ | 평균 시청 지속 시간 (mm:ss) |
| `avgUploadFrequency` | number | ✓ | 평균 업로드 횟수 (주 단위) |
| `impressionClickRate` | number | ✓ | 노출 대비 클릭률 (%) |

### ChannelValuation (채널 가치평가)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `channelId` | number | ✓ | 채널 ID (외래 키) |
| `overallGrade` | enum | ✓ | S \| A \| B \| C \| D |
| `overallScore` | number | ✓ | 종합 점수 (0~100) |
| `channelType` | string | ✓ | 채널 유형 (안정 배당형, 공격 성장형 등) |
| `radar` | object | ✓ | 성장성/안정성/수익성 (각각 grade, score) |
| `estimatedValue` | object | ✓ | { min, max } 추정 채널 가치 (원) |
| `appliedCpm` | number | ✓ | 적용 CPM (원) |
| `cpmCategory` | string | ✓ | CPM 적용 카테고리명 |
| `estimatedMonthlyRevenue` | object | ✓ | { min, max } 월 추정 수익 (원) |
| `calculatedAt` | Date | ✓ | 가치평가 계산 시각 |

### ChannelDetailMetrics (채널 세부 지표)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `channelId` | number | ✓ | 채널 ID |
| `growth` | object | ✓ | 성장성 지표 (grade, score, viewGrowthRateMoM, subscriberGrowthPerMonth, impressionClickThroughRate) |
| `stability` | object | ✓ | 안정성 지표 (grade, score, ctrVariance, uploadConsistency, averageViewDurationRate) |
| `profitability` | object | ✓ | 수익성 지표 (grade, score, estimatedMonthlyRevenueMin/Max, estimatedAnnualRevenueMin/Max, appliedCpm, cpmCategory) |
| `calculatedAt` | Date | ✓ | 지표 계산 시각 |

### ChannelSettings (채널 설정)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `channelId` | number | ✓ | 채널 기본 키 |
| `metricsPublic` | boolean | ✓ | 세부 지표 공개 여부 |
| `updatedAt` | Date | ✓ | 설정 마지막 변경 시각 |

---

## API 엔드포인트

### 채널 기본 정보 조회
```http
GET /api/creator/channels/{channelId}
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `channelId` | number | 채널 ID |

**응답 (200)**:
```json
{
  "data": {
    "id": 1,
    "channelName": "채널명 예시",
    "channelHandle": "@channel_id",
    "thumbnailUrl": "https://...",
    "connectionStatus": "CONNECTED",
    "reviewStatus": "UNREGISTERED",
    "lastSyncedAt": "2026-05-24T10:30:00Z",
    "stats": {
      "subscriberCount": 124000,
      "videoCount": 247,
      "totalViewCount": 324000
    },
    "details": {
      "category": "교육",
      "joinedAt": "2020-03-15",
      "avgMonthlyViews": 32400,
      "avgViewDuration": "05:23",
      "avgUploadFrequency": 2,
      "impressionClickRate": 4.8
    }
  },
  "error": null
}
```

**에러 응답**:
| 코드 | 상황 |
|------|------|
| 401 | 미인증 |
| 403 | 다른 크리에이터의 채널 접근 |
| 404 | 채널 없음 |

---

### 채널 가치평가 조회
```http
GET /api/creator/channels/{channelId}/valuation
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**응답 (200)**:
```json
{
  "data": {
    "channelId": 1,
    "overallGrade": "A",
    "overallScore": 72,
    "channelType": "균형 성장형",
    "radar": {
      "growth": { "grade": "A", "score": 85 },
      "stability": { "grade": "B", "score": 65 },
      "profitability": { "grade": "A", "score": 80 }
    },
    "estimatedValue": { "min": 150000000, "max": 200000000 },
    "appliedCpm": 3200,
    "cpmCategory": "교육 · 경제",
    "estimatedMonthlyRevenue": { "min": 8500000, "max": 12000000 },
    "calculatedAt": "2026-05-20T00:00:00Z"
  },
  "error": null
}
```

---

### 채널 세부 지표 조회
```http
GET /api/creator/channels/{channelId}/metrics
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**응답 (200)**:
```json
{
  "data": {
    "channelId": 1,
    "growth": {
      "grade": "A",
      "score": 80,
      "viewGrowthRateMoM": 12.5,
      "subscriberGrowthPerMonth": 1200,
      "impressionClickThroughRate": 4.8
    },
    "stability": {
      "grade": "B",
      "score": 65,
      "ctrVariance": 0.3,
      "uploadConsistency": 0.1,
      "averageViewDurationRate": 52.0
    },
    "profitability": {
      "grade": "A",
      "score": 75,
      "estimatedMonthlyRevenueMin": 100,
      "estimatedMonthlyRevenueMax": 133,
      "estimatedAnnualRevenueMin": 1200,
      "estimatedAnnualRevenueMax": 1596,
      "appliedCpm": 6.2,
      "cpmCategory": "교육"
    },
    "calculatedAt": "2026-05-24T00:00:00Z"
  },
  "error": null
}
```

---

### 채널 설정 조회
```http
GET /api/creator/channels/{channelId}/settings
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**응답 (200)**:
```json
{
  "data": {
    "channelId": 1,
    "metricsPublic": true,
    "updatedAt": "2026-05-24T10:00:00Z"
  },
  "error": null
}
```

---

### 채널 설정 변경
```http
PATCH /api/creator/channels/{channelId}/settings
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**요청 본문**:
```json
{
  "metricsPublic": false
}
```

**응답 (200)**:
```json
{
  "data": {
    "channelId": 1,
    "metricsPublic": false,
    "updatedAt": "2026-05-24T10:05:00Z"
  },
  "error": null
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 400 | 요청 본문 필드 타입 오류 |
| 401 | 미인증 |
| 403 | 다른 크리에이터의 채널 접근 |
| 404 | 채널 없음 |

---

### 채널 데이터 재동기화
```http
POST /api/creator/channels/{channelId}/sync
```

**인증 필요**: ✓ (크리에이터 — 자신의 채널만)

**요청 본문**: (없음)

**응답 (202 Accepted)**:
```json
{
  "data": {
    "channelId": 1,
    "status": "PENDING",
    "requestedAt": "2026-05-24T10:10:00Z"
  },
  "error": null
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 401 | 미인증 |
| 403 | 다른 크리에이터의 채널 접근 |
| 404 | 채널 없음 |
| 429 | 단기 재호출 요청 횟수 초과 |

---

## 비즈니스 로직

### 세부 지표 공개 설정
- `metricsPublic = true` → 투자자가 채널 상세 조회 시 세부 지표 포함
- `metricsPublic = false` → 투자자가 채널 상세 조회 시 세부 지표 미노출
- 크리에이터 본인은 항상 전체 데이터 조회 가능

### 데이터 재동기화 흐름
1. 크리에이터 "데이터 재호출" 버튼 탭
2. YouTube Data API / Analytics API 재호출
3. 채널 통계·세부정보 갱신
4. 가치평가 재계산 트리거 (비동기)
5. `lastSyncedAt` 갱신
6. 클라이언트에 갱신된 데이터 반환

### Rate Limit
- 동일 채널에 대한 과도한 재동기화 방지
- (정책 확인 필요)
