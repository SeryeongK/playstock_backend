# API 명세: 투자자 채널 목록 (SCR-I001)

> 투자자가 현재 거래 중인 채널과 오픈 예정인 채널을 탭으로 필터링하여 조회합니다.

---

## 데이터 모델

### Channel (채널)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 채널 고유 식별자 |
| `name` | string | ✓ | 채널 이름 |
| `status` | enum | ✓ | TRADING \| UPCOMING |
| `thumbnailUrl` | string | ✓ | 채널 썸네일 URL |
| `isPopular` | boolean | ✓ | 인기 채널 여부 |
| `description` | string | ○ | 채널 설명 |
| `createdAt` | Date | ✓ | 채널 생성 날짜 |
| `updatedAt` | Date | ✓ | 채널 마지막 수정 날짜 |

### ChannelCard (채널 카드 아이템)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `channelId` | UUID | ✓ | 채널 ID |
| `title` | string | ✓ | 제목 |
| `image` | string | ✓ | 썸네일 URL |
| `badge` | string | ○ | 배지 텍스트 (예: "소진임박") |
| `badgeColor` | enum | ○ | card-lavender \| card-mint \| card-pink \| card-teal \| card-yellow |

---

## API 엔드포인트

### 채널 목록 조회 (필터링)
```http
GET /api/channels
```

**인증 필요**: ✓

**쿼리 파라미터**:
| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `status` | string | ○ | TRADING 또는 UPCOMING |
| `page` | number | ○ | 페이지 번호 (기본: 1) |
| `limit` | number | ○ | 페이지당 항목 수 (기본: 10) |
| `popular` | boolean | ○ | true 시 인기 채널만 (최대 5개) |

**응답 (200)**:
```json
{
  "data": [
    {
      "id": "uuid",
      "name": "channel-name",
      "status": "TRADING",
      "thumbnailUrl": "https://...",
      "isPopular": true,
      "description": "채널 설명",
      "cards": [
        {
          "channelId": "uuid",
          "title": "제목",
          "image": "https://...",
          "badge": "소진임박",
          "badgeColor": "card-pink"
        }
      ],
      "createdAt": "2026-05-19T00:00:00Z",
      "updatedAt": "2026-05-19T00:00:00Z"
    }
  ],
  "meta": {
    "total": 15,
    "page": 1,
    "limit": 10,
    "pages": 2
  }
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 400 | 잘못된 쿼리 파라미터 |
| 401 | 미인증 |
| 403 | 권한 없음 |

---

### 인기 채널 조회 (캐러셀용)
```http
GET /api/channels?popular=true
```

**인증 필요**: ✓

**응답 (200)**:
```json
{
  "data": [
    {
      "id": "uuid",
      "name": "인기채널1",
      "status": "TRADING",
      "thumbnailUrl": "https://...",
      "isPopular": true
    },
    {
      "id": "uuid",
      "name": "인기채널2",
      "status": "TRADING",
      "thumbnailUrl": "https://..."
    }
  ],
  "meta": {
    "total": 2,
    "limit": 5
  }
}
```

---

## 비즈니스 로직

### 탭 필터링
- **거래 채널 탭**: `status = "TRADING"` 필터, 3개 리스트 아이템 표시
- **오픈 예정 탭**: `status = "UPCOMING"` 필터, 2개 리스트 아이템 표시

### 페이지네이션
- 기본 페이지당 10개 항목
- "더보기" 버튼 클릭 시 다음 페이지로 이동

### 인기 채널 (캐러셀)
- `isPopular = true`인 채널만 표시
- 최대 5개까지만 반환
- 거래/오픈 상태 관계없이 모두 표시 가능

---

## 유효성 검사

| 필드 | 규칙 |
|------|------|
| `status` | TRADING 또는 UPCOMING |
| `page` | 1 이상의 정수 |
| `limit` | 1~100 사이의 정수 |
| `popular` | boolean |
