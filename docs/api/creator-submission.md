# API 명세: 크리에이터 심사 제출 (SCR-C003)

> 크리에이터가 채널 심사를 위한 서류(PDF)를 업로드하고 심사 신청을 완료합니다.

---

## 데이터 모델

### DocumentType (서류 종류)
```
ADSENSE_STATEMENT       - AdSense 지급 명세서 (필수)
YOUTUBE_STUDIO_REPORT   - YouTube Studio 수익 리포트 (선택)
INCOME_CERTIFICATE      - 소득 금액 증명원 (선택)
BUSINESS_REGISTRATION   - 사업자 등록증 (조건부)
```

### EntityType (채널 운영 주체)
```
INDIVIDUAL - 개인
CORPORATE  - 법인·사업자
```

### ChannelDocument (서류 업로드 결과)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 서류 고유 식별자 |
| `channelId` | UUID | ✓ | 연결된 채널 ID |
| `documentType` | enum | ✓ | DocumentType |
| `fileName` | string | ✓ | 업로드된 파일명 |
| `fileSize` | number | ✓ | 파일 크기 (bytes) |
| `uploadedAt` | Date | ✓ | 업로드 시각 |

### ChannelApplication (심사 신청)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 신청 고유 식별자 |
| `channelId` | UUID | ✓ | 신청 채널 ID |
| `entityType` | enum | ✓ | INDIVIDUAL \| CORPORATE |
| `status` | enum | ✓ | SUBMITTED \| IN_REVIEW \| APPROVED \| REJECTED |
| `submittedAt` | Date | ✓ | 신청 제출 시각 |

---

## API 엔드포인트

### 필요 서류 목록 조회
```http
GET /api/creator/channels/me/document-requirements
```

**인증 필요**: ✓ (크리에이터)

**응답 (200)**:
```json
{
  "data": [
    {
      "documentType": "ADSENSE_STATEMENT",
      "name": "AdSense 지급 명세서",
      "issuer": "Google AdSense",
      "requirement": "REQUIRED"
    },
    {
      "documentType": "YOUTUBE_STUDIO_REPORT",
      "name": "YouTube Studio 수익 리포트",
      "issuer": "YouTube Studio",
      "requirement": "OPTIONAL",
      "hint": "최근 6개월 이내 자료"
    },
    {
      "documentType": "INCOME_CERTIFICATE",
      "name": "소득 금액 증명원",
      "issuer": "국세청 홈택스",
      "requirement": "OPTIONAL",
      "hint": "PDF 업로드 · 직전 연도 발행본 · 공식 발급본만 허용"
    },
    {
      "documentType": "BUSINESS_REGISTRATION",
      "name": "사업자 등록증",
      "issuer": "국세청",
      "requirement": "CONDITIONAL"
    }
  ],
  "error": null
}
```

**Requirement 값**:
| 값 | 의미 |
|----|------|
| REQUIRED | 필수 — 반드시 제출 |
| OPTIONAL | 선택 — 없어도 신청 가능 |
| CONDITIONAL | 조건부 — 법인(CORPORATE) 선택 시 필수 |

---

### 서류 파일 업로드
```http
POST /api/creator/channels/me/documents
```

**인증 필요**: ✓ (크리에이터)

**요청 형식**: `multipart/form-data`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `documentType` | string | ✓ | DocumentType 값 |
| `file` | file | ✓ | PDF 파일 |

**응답 (201)**:
```json
{
  "data": {
    "id": "uuid",
    "channelId": "uuid",
    "documentType": "ADSENSE_STATEMENT",
    "fileName": "AdSense 지급 명세서_@Channel name.pdf",
    "fileSize": 10240,
    "uploadedAt": "2026-05-24T10:00:00Z"
  },
  "error": null
}
```

**에러**:
| 코드 | 메시지 |
|------|--------|
| `INVALID_FILE_TYPE` | PDF 파일만 업로드 가능합니다 |
| `FILE_TOO_LARGE` | 파일 크기를 초과했습니다 |
| `DOCUMENT_ALREADY_UPLOADED` | 이미 업로드된 서류입니다 |

---

### 서류 삭제
```http
DELETE /api/creator/channels/me/documents/{documentId}
```

**인증 필요**: ✓ (크리에이터)

**응답 (200)**:
```json
{
  "data": { "deleted": true },
  "error": null
}
```

**에러**:
| 코드 | 메시지 |
|------|--------|
| `DOCUMENT_NOT_FOUND` | 서류를 찾을 수 없습니다 |
| `APPLICATION_ALREADY_SUBMITTED` | 이미 제출된 심사입니다 |

---

### 심사 신청 제출
```http
POST /api/creator/channels/me/applications
```

**인증 필요**: ✓ (크리에이터)

**요청 본문**:
```json
{
  "entityType": "INDIVIDUAL"
}
```

**응답 (201)**:
```json
{
  "data": {
    "id": "uuid",
    "channelId": "uuid",
    "entityType": "INDIVIDUAL",
    "status": "SUBMITTED",
    "submittedAt": "2026-05-24T10:00:00Z"
  },
  "error": null
}
```

**에러**:
| 코드 | 메시지 |
|------|--------|
| `REQUIRED_DOCUMENT_MISSING` | 필수 서류가 누락되었습니다 |
| `CORPORATE_DOCUMENT_MISSING` | 법인 필수 서류가 누락되었습니다 |
| `APPLICATION_ALREADY_EXISTS` | 이미 심사 신청이 존재합니다 |
| `CHANNEL_NOT_CONNECTED` | 채널이 연결되지 않았습니다 |

---

## 비즈니스 로직

### 서류 요구사항
- **파일 형식**: PDF 전용
- **유효 기간**: 최근 6개월 이내 발급 자료 (프론트 안내 문구만 존재, 서버 검증 여부 미확인)

### 운영 주체별 필수 서류
- **INDIVIDUAL**: `REQUIRED` 서류만 제출 (AdSense 지급 명세서)
- **CORPORATE**: `REQUIRED` + `CONDITIONAL` 서류 모두 필수 (AdSense + 사업자등록증)

### 심사 신청 제출 규칙
- 심사 신청 후에는 서류 삭제/재업로드 불가
- 심사 승인(APPROVED) 시 채널 status가 ACTIVE로 전환 (별도 관리자 API)

---

## 유효성 검사

| 필드 | 규칙 |
|------|------|
| `entityType` | INDIVIDUAL \| CORPORATE |
| 서류 파일 | PDF만 허용 |
| 파일 크기 | (상한 미정) |
| 중복 업로드 | 동일 documentType 중복 업로드 불가 |
