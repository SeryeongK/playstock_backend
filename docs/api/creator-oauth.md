# API 명세: 크리에이터 OAuth 연결 (SCR-C001)

> 크리에이터가 자신의 유튜브 채널을 Google OAuth로 연결하여 플랫폼에 등록합니다.

---

## 데이터 모델

### YoutubeOAuthConnect (OAuth 연결 요청)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `redirectUrl` | string | ✓ | Google OAuth 동의 화면 URL |

### YoutubeChannel (연결 완료 후 등록된 채널)
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `id` | UUID | ✓ | 채널 고유 식별자 |
| `youtubeChannelId` | string | ✓ | YouTube 채널 ID |
| `name` | string | ✓ | 채널 이름 |
| `thumbnailUrl` | string | ✓ | 채널 썸네일 URL |
| `subscriberCount` | number | ✓ | 구독자 수 |
| `status` | ChannelStatus | ✓ | 채널 상태 (초기값: `PENDING`) |
| `creatorId` | UUID | ✓ | 채널 소유 크리에이터 ID |
| `connectedAt` | Date | ✓ | OAuth 연결 완료 시각 |

**ChannelStatus**: `PENDING | ACTIVE | SOLD_OUT | EXPIRED | SUSPENDED`

---

## API 엔드포인트

### OAuth 리디렉션 URL 요청
```http
GET /api/oauth/youtube/connect
```

**인증 필요**: ✓ (크리에이터 계정)

**응답 (200)**:
```json
{
  "data": {
    "redirectUrl": "https://accounts.google.com/o/oauth2/auth?..."
  },
  "error": null
}
```

**역할**: 백엔드가 Google OAuth 동의 화면 URL을 생성하여 반환합니다.
프론트엔드는 `window.location.href`로 리디렉션합니다.

---

### OAuth 콜백 처리
```http
GET /api/oauth/youtube/callback?code={authCode}&state={state}
```

**인증 필요**: ✗ (Google에서 리디렉션)

**쿼리 파라미터**:
| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `code` | string | Google OAuth 인증 코드 |
| `state` | string | CSRF 방지용 상태값 |

**동작 흐름**:
1. Google로부터 액세스 토큰 획득
2. YouTube Data API로 채널 정보 조회
3. 채널 DB 등록 (status: `PENDING`)
4. 크리에이터 채널 상세 페이지로 리디렉션

**에러 케이스**:
| 코드 | 메시지 |
|------|--------|
| `OAUTH_DENIED` | 권한 승인이 거부되었습니다 |
| `CHANNEL_ALREADY_CONNECTED` | 이미 연결된 채널입니다 |
| `INVALID_STATE` | 유효하지 않은 요청입니다 |

---

## 비즈니스 로직

- **OAuth 스코프**: `youtube.readonly` (조회 전용, 관리 권한 없음)
- **초기 상태**: 연결 완료 후 `PENDING` → 운영자 심사 후 `ACTIVE` 전환
- **제한사항**: 한 크리에이터당 채널 1개만 연결 가능 (추후 변경 가능)
