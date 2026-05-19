---
feature: F005
title: YouTube OAuth 채널 연동
status: 🟡 진행 중
started: 2026-05-19
finished: -
---

# F005: YouTube OAuth 채널 연동

**상태:** 🟡 진행 중
**기간:** 2026-05-19 ~ -
**관련 화면:** 없음 (API 전용)
**관련 PR:** feat/channel 브랜치
**관련 ADR:** -

---

## 1. 설계 (Why & What)

### 목적
크리에이터가 Google OAuth를 통해 YouTube 채널을 플랫폼에 연동한다. 공개 API로는 수집 불가한 실제 수익·CPM·RPM·시청시간 등 Analytics 데이터를 OAuth 인증 후 주기적으로 자동 수집하여, AI 가치평가 및 사기 탐지의 원천 데이터를 확보한다.

### 입력 / 출력
- 입력: Google OAuth 인가코드 (callback), JWT (connect/status/disconnect)
- 출력: Google 동의화면 URL, 연동 상태(youtubeChannelId + 토큰 만료시각), channel_metrics 누적 row

### 핵심 흐름
1. 크리에이터가 `GET /api/oauth/youtube/connect` 호출 → UUID state 생성 후 인메모리 Map 저장, Google OAuth URL 반환
2. Google 동의화면 → `GET /api/oauth/youtube/callback?code=...&state=...` 리다이렉트
3. state 검증 → Google Token API로 access_token + refresh_token 교환
4. AES-256-GCM으로 토큰 암호화 → users 테이블 OAuth 컬럼 4개 저장
5. YouTube Data API(`mine=true`)로 채널 기본 정보 수집 → `users.youtube_channel_id` 저장
6. channels 테이블에 해당 채널이 이미 등록되어 있으면 channel_metrics row INSERT
7. 프론트엔드로 redirect (성공/실패 쿼리파라미터 포함)

### 주요 결정
- Google API 클라이언트 라이브러리 대신 RestClient 직접 구현 (의존성 최소화)
- Tink 대신 javax.crypto AES-256-GCM 사용 (단순성 우선)
- state는 인메모리 ConcurrentHashMap 저장 (MVP 단일 인스턴스 한정. 서버 재시작 시 소실됨)
- Analytics API 실패 시 예외 전파 대신 `Optional.empty()` 반환 (수집 실패가 전체 흐름 차단 방지)

---

## 2. 구현 (How)

### 패키지
`com.playstock.user.oauth`, `com.playstock.infra.google`, `com.playstock.infra.youtube`, `com.playstock.channel`

### 파일 목록

**신규**
- `infra/google/GoogleOAuthProperties.java` — `google.oauth.*` 설정 바인딩 (`@ConfigurationProperties`)
- `infra/google/TokenEncryptionService.java` — AES-256-GCM 암복호화. 키는 환경변수 `GOOGLE_TOKEN_ENCRYPTION_KEY`
- `infra/google/GoogleOAuthService.java` — OAuth URL 생성, 토큰 교환(code→token), access_token 갱신
- `infra/youtube/YouTubeDataApiClient.java` — Bearer 토큰으로 `mine=true` 채널 조회, 재생목록 영상 통계 수집
- `infra/youtube/YouTubeAnalyticsClient.java` — 최근 28일 수익/CPM/RPM/시청시간 등 집계
- `infra/youtube/YouTubeCreatorChannelInfo.java` — 채널 정보 응답 레코드
- `infra/youtube/YouTubeVideoMetrics.java` — 영상별 통계 응답 레코드
- `infra/youtube/YouTubeAnalyticsMetrics.java` — Analytics 집계 응답 레코드
- `channel/ChannelMetrics.java` — channel_metrics 엔티티
- `channel/ChannelMetricsRepository.java` — JPA Repository
- `channel/YouTubeDataSyncScheduler.java` — 주간 스케줄러 (매주 일요일 02:00)
- `user/oauth/YouTubeOAuthService.java` — OAuth 연동 유스케이스 서비스
- `user/oauth/OAuthController.java` — 컨트롤러 (4개 엔드포인트)
- `user/oauth/OAuthConnectUrlResponse.java` — 동의화면 URL 응답 DTO
- `user/oauth/OAuthStatusResponse.java` — 연동 상태 응답 DTO

**수정**
- `db/migration/V2__add_oauth_and_analytics_columns.sql` — users OAuth 컬럼 4개, channel_metrics Analytics 컬럼 9개 추가
- `application.yml` — `google.oauth.*` 설정 블록 추가
- `PlaystockBackendApplication.java` — `@EnableScheduling` 추가
- `user/User.java` — OAuth 필드 4개 + `connectYouTube` / `disconnectYouTube` / `updateAccessToken` / `isYouTubeConnected` 메서드
- `user/UserRepository.java` — `findByYoutubeChannelId`, `findAllByYoutubeChannelIdIsNotNull` 추가
- `common/exception/ErrorCode.java` — OAuth 관련 에러코드 6개 추가
- `common/config/SecurityConfig.java` — `/api/oauth/youtube/callback` public 허용

### API
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/oauth/youtube/connect` | Bearer JWT | Google 동의화면 URL 반환 |
| GET | `/api/oauth/youtube/callback` | public | Google 리다이렉트 콜백. 토큰 교환 후 프론트엔드로 redirect |
| GET | `/api/oauth/youtube/status` | Bearer JWT | 연동 상태 (youtubeChannelId + 토큰 만료시각) |
| DELETE | `/api/oauth/youtube/disconnect` | Bearer JWT | 연동 해제. OAuth 컬럼 null 처리 |

### DB 변경
`V2__add_oauth_and_analytics_columns.sql`

- **users** 컬럼 4개 추가: `google_oauth_access_token`, `google_oauth_refresh_token`, `google_oauth_token_expires_at`, `youtube_channel_id`
- **channel_metrics** Analytics 컬럼 9개 추가: `estimated_revenue`, `cpm`, `rpm`, `watch_time_minutes`, `avg_view_duration`, `impressions`, `impression_ctr`, `subscribers_gained`, `subscribers_lost`

### 의존성
- 선행 기능: F003 (JWT 인증), F004 (채널 등록 — channel_metrics 저장 대상)
- 외부 API: Google OAuth 2.0 Token API, YouTube Data API v3 (`mine=true`), YouTube Analytics API
- 라이브러리: Spring Web (RestClient), javax.crypto (AES-256-GCM), Spring Scheduler

### 핵심 코드 (스니펫)
```java
// TokenEncryptionService — AES-256-GCM 암호화 (랜덤 IV 생성 후 암호문 앞에 prepend)
public String encrypt(String plaintext) {
    byte[] iv = new byte[12];
    new SecureRandom().nextBytes(iv);
    GCMParameterSpec spec = new GCMParameterSpec(128, iv);
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);
    byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    byte[] combined = new byte[iv.length + encrypted.length];
    System.arraycopy(iv, 0, combined, 0, iv.length);
    System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
    return Base64.getEncoder().encodeToString(combined);
}
```

---

## 3. 검증 (Verify)

### 테스트
- 별도 테스트 없음 (수동 검증으로 대체)

### 수동 검증
- [ ] `GET /api/oauth/youtube/connect` — 유효 JWT: 200 + Google 동의화면 URL 반환
- [ ] `GET /api/oauth/youtube/connect` — 토큰 없음: 403
- [ ] Google 동의화면 통과 → callback 정상 처리 후 프론트엔드로 redirect (`?success=true`)
- [ ] `GET /api/oauth/youtube/status` — 연동 후: youtubeChannelId + 만료시각 반환
- [ ] `DELETE /api/oauth/youtube/disconnect` — OAuth 컬럼 null, youtube_channel_id null 확인
- [ ] callback state 불일치 → redirect `?success=false&error=INVALID_OAUTH_STATE`
- [ ] 주간 스케줄러 수동 트리거 → channel_metrics 새 row INSERT 확인

### Swagger
- `/swagger-ui.html` → "OAuth" 태그

### 환경변수
| 변수명 | 설명 |
|--------|------|
| `GOOGLE_CLIENT_ID` | Google Cloud Console OAuth 클라이언트 ID |
| `GOOGLE_CLIENT_SECRET` | OAuth 클라이언트 시크릿 |
| `GOOGLE_REDIRECT_URI` | 기본값: `http://localhost:8080/api/oauth/youtube/callback` |
| `GOOGLE_TOKEN_ENCRYPTION_KEY` | `openssl rand -base64 32`로 생성한 32바이트 키 |
| `FRONTEND_URL` | 기본값: `http://localhost:3000` |

---

## 4. 후속 (Follow-up)

### 알려진 한계
- state를 인메모리 Map에 저장하므로 서버 재시작 시 진행 중인 OAuth 세션이 소실됨. 다중 인스턴스 환경에서 사용 불가. (MVP 단일 인스턴스 한정)
- Analytics API는 Google Cloud Console에 테스트 계정으로 등록된 채널만 수집 가능 (실서비스 시 Google 보안 심사 필요)
- access_token 갱신 실패 시 해당 채널은 이번 사이클 수집 skip (알림 없음)

### 미해결
- 스케줄러 수집 실패 시 운영자 알림 미구현
- refresh_token 만료(6개월 미사용) 처리 — 재연동 유도 알림 미구현
- 채널 등록(F004) 전 OAuth 연동 완료한 경우, 등록 시점에 channel_metrics 자동 수집 트리거 미구현

### 트러블슈팅 기록
- `application.yml` 인덴트 오류: `google:` 블록 추가 시 기존 `jwt:`, `ai:` 항목이 하위로 밀림 → 최상위 블록으로 분리하여 해결
- `@RequestParam` 이름 미지정 오류: `-parameters` 컴파일러 플래그 없을 때 파라미터 이름 추론 실패 → `@RequestParam("code")` 명시로 해결
- state 검증 실패: 서버 재시작으로 인메모리 state 소실 → `/connect` 재호출로 해결

### 학습 포인트
- AES-256-GCM 암복호화 구조 (IV prepend 방식)
- OAuth 2.0 Authorization Code Flow (code → token 교환, refresh_token으로 갱신)
- `@ConfigurationProperties` 바인딩

### 향후 개선
- state 저장소를 Redis 또는 DB로 교체 (다중 인스턴스 대응)
- 스케줄러 수집 실패 알림 → notifications 테이블 연동
- YouTube Analytics API 실서비스 전환 시 Google 보안 심사 대응
