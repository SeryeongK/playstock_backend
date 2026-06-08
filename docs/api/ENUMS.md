# 열거형(Enum) 정의

> 모든 열거형 값을 한 곳에 정리한 문서입니다.

---

## 채널 관련

### ChannelStatus (채널 상태)
```
PENDING   - 심사 중 (UI: 회색 배지)
ACTIVE    - 정상 판매 중 (UI: 초록 배지)
SOLD_OUT  - 완판 (UI: 파란 배지)
EXPIRED   - 만기 종료 (UI: 회색 배지)
SUSPENDED - 정지 (UI: 빨간 배지)
```

### ChannelTier (채널 등급)
```
BRONZE - 청동 색상: #CD7F32
SILVER - 은색 색상: #C0C0C0
GOLD   - 금색 색상: #FFD700
```

### ChannelType (채널 유형 — 가치평가 기반)
```
안정 배당형   - 수익성 A↑ + 안정성 A↑ → 테마: #0A8C71
공격 성장형   - 성장성 A↑ + 수익성 C↓ → 테마: #E51D1D
균형 성장형   - 3축 모두 B↑ → 테마: #0A49EB
수익 집중형   - 수익성 S + 성장성 C↓ → 테마: #E51D1D
리스크 주의형 - 안정성 D ∨ 수익성 D → 테마: #C55109
관찰 필요형   - 데이터 부족 ∨ 운영 < 6개월 → 테마: #C55109
```

### ChannelGrade (등급 점수)
```
S - 최상위 (85~100점)
A - 우수 (70~84점)
B - 양호 (55~69점)
C - 보통 (40~54점)
D - 주의 (40점 미만)
```

### ChannelConnectionStatus
```
CONNECTED    - 연동 완료
DISCONNECTED - 연동 해제
```

### ChannelReviewStatus (심사 등록 상태)
```
UNREGISTERED - 심사 미등록 (UI: 분홍 dot)
REGISTERED   - 심사 등록 완료 대기
UNDER_REVIEW - 심사 중
APPROVED     - 심사 승인
REJECTED     - 심사 반려
```

### BadgeColor (카드 배지 색상)
```
card-lavender
card-mint
card-pink
card-teal
card-yellow
```

---

## 크리에이터 관련

### DocumentType (서류 종류)
```
ADSENSE_STATEMENT     - AdSense 지급 명세서 (필수)
YOUTUBE_STUDIO_REPORT - YouTube Studio 수익 리포트 (선택)
INCOME_CERTIFICATE    - 소득 금액 증명원 (선택)
BUSINESS_REGISTRATION - 사업자 등록증 (조건부)
```

### DocumentRequirement
```
REQUIRED   - 필수 제출
OPTIONAL   - 선택 제출
CONDITIONAL - 조건부 (운영 주체 선택에 따라)
```

### EntityType (채널 운영 주체)
```
INDIVIDUAL - 개인
CORPORATE  - 법인·사업자
```

### ApplicationStatus (심사 신청 상태)
```
SUBMITTED  - 제출 완료 (심사 대기)
IN_REVIEW  - 심사 중
APPROVED   - 승인
REJECTED   - 반려
```

---

## 투자자 관련

### PurchaseStatus (구매 주문 상태)
```
PENDING   - 결제 대기
COMPLETED - 구매 완료
FAILED    - 구매 실패
CANCELLED - 구매 취소
```

### PaymentMethod (결제 수단)
```
card                  - 신용카드
account_transfer      - 계좌이체
(추가 수단 확정 필요)
```

### InvestmentType (투자 성향)
```
STABLE_DIVIDEND   - 안정 배당형
AGGRESSIVE_GROWTH - 공격 성장형
```

---

## 목록 조회 쿼리 파라미터

### status (필터)
```
TRADING   - 거래 중
UPCOMING  - 오픈 예정
```

### tab (채널 상세 탭)
```
fragment   - 조각정보 (기본값)
valuation  - 가치평가
revenue    - 수익 구조
```

---

## 에러 코드

### OAuth
```
OAUTH_DENIED              - 권한 승인 거부
CHANNEL_ALREADY_CONNECTED - 이미 연결된 채널
INVALID_STATE             - CSRF 토큰 불일치
```

### 서류 업로드
```
INVALID_FILE_TYPE         - PDF 이외 파일
FILE_TOO_LARGE            - 파일 크기 초과
DOCUMENT_ALREADY_UPLOADED - 동일 서류 중복 업로드
DOCUMENT_NOT_FOUND        - 서류 없음
APPLICATION_ALREADY_SUBMITTED - 심사 이미 제출됨
```

### 심사 신청
```
REQUIRED_DOCUMENT_MISSING     - 필수 서류 미업로드
CORPORATE_DOCUMENT_MISSING    - 법인 필수 서류 미업로드
APPLICATION_ALREADY_EXISTS    - 심사 신청 중복
CHANNEL_NOT_CONNECTED         - 채널 미연동
```

### 구매
```
INVALID_FILE_TYPE      - (문서 업로드 관련)
FILE_TOO_LARGE         - (문서 업로드 관련)
(상세 에러 정의는 구매 관련 문서 참조)
```

---

## 색상 코드 참조

| 용도 | 색상 | 코드 |
|------|------|------|
| 안정 배당형 | 민트 | #0A8C71 |
| 공격 성장형 | 빨강 | #E51D1D |
| 균형 성장형 | 파랑 | #0A49EB |
| 리스크 주의형 | 주황 | #C55109 |
| 기본 민트 (연동) | 민트 | #44C3A2 |
| 경고 (심사 미등록) | 핑크 | #FFA0A0 |
| Bronze 등급 | 청동 | #CD7F32 |
| Silver 등급 | 은색 | #C0C0C0 |
| Gold 등급 | 금색 | #FFD700 |
