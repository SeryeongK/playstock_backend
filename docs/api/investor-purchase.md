# API 명세: 투자자 구매 (SCR-I003, SCR-I005)

> 투자자가 원하는 수량의 조각을 선택하고 구매를 진행합니다.

---

## 데이터 모델

### Purchase (구매 주문)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 구매 주문 ID |
| `userId` | UUID | ✓ | 사용자 ID |
| `channelId` | UUID | ✓ | 채널 ID |
| `quantity` | number | ✓ | 구매 수량 |
| `unitPrice` | number | ✓ | 단가 (구매 시점) |
| `totalPrice` | number | ✓ | 총 가격 = quantity × unitPrice |
| `status` | enum | ✓ | PENDING \| COMPLETED \| FAILED \| CANCELLED |
| `paymentMethod` | string | ✓ | card \| account_transfer |
| `transactionId` | string | ✓ | 결제 거래 ID |
| `createdAt` | Date | ✓ | 주문 생성 날짜 |
| `completedAt` | Date | ○ | 완료 날짜 |

### PurchaseCart (장바구니)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `channelId` | UUID | ✓ | 채널 ID |
| `quantity` | number | ✓ | 원하는 수량 |
| `unitPrice` | number | ✓ | 현재 단가 |

---

## API 엔드포인트

### 구매 정보 미리보기
```http
GET /api/purchases/preview/:channelId
```

**인증 필요**: ✓

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `channelId` | UUID | 채널 ID |

**응답 (200)**:
```json
{
  "data": {
    "channelId": "uuid",
    "channelName": "뜬뜬 DdeunDdeun",
    "channelThumbnail": "https://...",
    "unitPrice": 2500,
    "availableQuantity": 5000,
    "saleProgress": 66,
    "minQuantity": 1,
    "maxQuantity": 100,
    "quickSelectOptions": [1, 5, 10, 20]
  }
}
```

---

### 구매 가격 계산
```http
POST /api/purchases/calculate
```

**인증 필요**: ✓

**요청 본문**:
```json
{
  "channelId": "uuid",
  "quantity": 10
}
```

**응답 (200)**:
```json
{
  "data": {
    "channelId": "uuid",
    "quantity": 10,
    "unitPrice": 2500,
    "subtotal": 25000,
    "discount": 0,
    "fee": 0,
    "tax": 0,
    "totalPrice": 25000,
    "message": "25,000원 결제하기"
  }
}
```

---

### 구매 주문 생성 (결제 전)
```http
POST /api/purchases
```

**인증 필요**: ✓

**요청 본문**:
```json
{
  "channelId": "uuid",
  "quantity": 10,
  "totalPrice": 25000,
  "paymentMethod": "card"
}
```

**응답 (201)**:
```json
{
  "data": {
    "id": "purchase_uuid",
    "status": "PENDING",
    "redirectUrl": "https://payment-gateway.example.com/...",
    "transactionId": "tx_12345"
  }
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 400 | 잘못된 수량/가격 |
| 401 | 미인증 |
| 404 | 채널 없음 |
| 409 | 판매 수량 부족 |

---

### 구매 완료 (결제 후 콜백)
```http
POST /api/purchases/:id/complete
```

**인증 필요**: ✓

**경로 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `id` | UUID | 구매 주문 ID |

**요청 본문**:
```json
{
  "transactionId": "tx_12345",
  "paymentConfirmation": "confirmation_code"
}
```

**응답 (200)**:
```json
{
  "data": {
    "id": "purchase_uuid",
    "status": "COMPLETED",
    "channelId": "uuid",
    "channelName": "뜬뜬 DdeunDdeun",
    "quantity": 10,
    "totalPrice": 25000,
    "completedAt": "2026-05-19T12:34:56Z",
    "nextSteps": [
      {
        "label": "채널 상세 보기",
        "route": "/channels/uuid"
      },
      {
        "label": "마이페이지",
        "route": "/mypage"
      }
    ]
  }
}
```

**에러**:
| 코드 | 상황 |
|------|------|
| 401 | 미인증 |
| 404 | 주문 없음 |
| 422 | 결제 검증 실패 |

---

## 비즈니스 로직

### 수량 선택 & 가격 계산
- Stepper 또는 Quick Select로 수량 선택
- 실시간으로 `POST /api/purchases/calculate` 호출
- UI에서 동적으로 가격 업데이트

### Quick Select 옵션
- 미리 정의된 수량: [1, 5, 10, 20]

### 수량 제한
- 최소: 1개
- 최대: 100개 (또는 판매 가능한 전체 수량)

### 결제 플로우
1. 사용자가 "결제하기" 클릭
2. `POST /api/purchases` 호출 → 구매 주문 생성 (status: PENDING)
3. 결제 게이트웨이로 리다이렉트
4. 결제 완료 후 콜백
5. `POST /api/purchases/:id/complete` 호출 → 상태 변경 (status: COMPLETED)
6. SCR-I005 구매 완료 페이지로 이동

### 구매 완료 시 부수 효과
- 사용자의 보유 자산 업로드
- 채널의 총 판매 수량 업데이트
- 배당금 계산 시작

---

## 유효성 검사

| 필드 | 규칙 |
|------|------|
| `channelId` | UUID 형식 |
| `quantity` | 1 이상의 정수 |
| `quantity` | 최대값 이하 |
| `totalPrice` | quantity × unitPrice와 일치 |
| `paymentMethod` | card \| account_transfer |
