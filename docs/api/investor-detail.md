# API 명세: 투자자 채널 상세 (SCR-I002)

> 투자자가 특정 채널의 상세 정보를 조각정보, 가치평가, 수익 구조 탭으로 조회합니다.

---

## 데이터 모델

### Channel (기본 정보)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 채널 ID |
| `name` | string | ✓ | 채널명 |
| `thumbnailUrl` | string | ✓ | 썸네일 URL |
| `description` | string | ○ | 채널 설명 |
| `isLiked` | boolean | ✓ | 찜 여부 (사용자별) |
| `likesCount` | number | ✓ | 총 찜 개수 |
| `createdAt` | Date | ✓ | 채널 생성 날짜 |

### ChannelDetail (탭별 상세 정보)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `fragmentPrice` | number | ✓ | 조각 단가 |
| `fragmentCount` | number | ✓ | 보유 조각 수 |
| `totalFragments` | number | ✓ | 전체 조각 수 |
| `saleProgress` | number | ✓ | 판매 진행률 (0-100) |
| `saleEndDate` | Date | ✓ | 판매 종료 예정일 |
| `grassInfo` | object[] | ○ | 조각 잔디 정보 |
| `aiValuation` | object | ✓ | AI 가치평가 (score, gradeLabel, description) |
| `categoryComparison` | object | ✓ | 카테고리 비교 데이터 |
| `monthlyRevenue` | number | ✓ | 예상 월 수익 |
| `revenueBreakdown` | object[] | ✓ | 수익 항목별 상세 |
| `investmentType` | enum | ✓ | STABLE_DIVIDEND \| AGGRESSIVE_GROWTH |

---

## API 엔드포인트

### 채널 상세 정보 조회
```http
GET /api/channels/:id
```

**인증 필요**: ✓

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | UUID | 채널 ID |

**쿼리 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `tab` | string | fragment \| valuation \| revenue (기본: fragment) |

**응답 (200)**:
```json
{
  "data": {
    "id": "uuid",
    "name": "뜬뜬 DdeunDdeun",
    "thumbnailUrl": "https://...",
    "description": "채널 설명",
    "isLiked": false,
    "likesCount": 1234,
    "detail": {
      "fragmentPrice": 2500,
      "fragmentCount": 100,
      "totalFragments": 10000,
      "saleProgress": 66,
      "saleEndDate": "2026-06-30T00:00:00Z",
      "grassInfo": [
        {
          "title": "조각 잔디",
          "imageUrl": "https://..."
        }
      ],
      "aiValuation": {
        "score": 85,
        "gradeLabel": "A",
        "description": "높은 성장 잠재력"
      },
      "categoryComparison": {
        "categories": ["카테고리1", "카테고리2"],
        "values": [85, 78]
      },
      "monthlyRevenue": 1980,
      "revenueBreakdown": [
        {
          "category": "광고 수익",
          "amount": 1200,
          "percentage": 60
        },
        {
          "category": "후원 수익",
          "amount": 600,
          "percentage": 30
        }
      ],
      "investmentType": "STABLE_DIVIDEND"
    },
    "createdAt": "2026-01-01T00:00:00Z"
  }
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 400 | 잘못된 채널 ID 형식 |
| 401 | 미인증 |
| 404 | 채널 없음 |

---

### 채널 찜하기 토글
```http
POST /api/channels/:id/like
```

**인증 필요**: ✓

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | UUID | 채널 ID |

**요청 본문**: (없음)

**응답 (200)**:
```json
{
  "data": {
    "isLiked": true,
    "likesCount": 1235
  }
}
```

---

## 비즈니스 로직

### 탭별 데이터
- **조각정보 탭**: 가격, 판매 진행률, 판매 종료일 등 거래 정보
- **가치평가 탭**: AI 기반 평가 점수 및 카테고리 비교
- **수익 구조 탭**: 예상 월 수익, 수익 항목별 분석, 투자 성향

모든 탭 데이터는 한 번의 API 호출로 전달되며, 클라이언트에서 탭 선택에 따라 표시합니다.

### 찜하기
- 사용자가 하트 아이콘 클릭 시 찜 상태 토글
- UI에서 즉시 반영 (낙관적 업데이트)
- 백엔드에서 비동기로 저장

### 수익 구조 계산
- 월 수익 = 예상 수익 항목 합계
- 각 항목의 백분율 합계 = 100%

---

## 유효성 검사

| 필드 | 규칙 |
|------|------|
| `id` | UUID 형식 |
| `tab` | fragment \| valuation \| revenue |
| `fragmentPrice` | 0 초과 |
| `saleProgress` | 0~100 사이 정수 |
