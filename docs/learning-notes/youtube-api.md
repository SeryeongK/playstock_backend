# YouTube API 학습 노트

> Java/Spring 전환 학습 기록 / 핀넥트 프로젝트 중 정리

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-05-20

### Q. YouTube Data API를 호출하면 바로 지표 값을 알 수 있나요?

**A.** API 종류에 따라 다릅니다.

**YouTube Data API v3 (API 키만 필요)**
- 공개 데이터만 제공
- 구독자 수, 누적 조회수, 영상 수 등 즉시 조회 가능
- 수익, CPM, 광고 데이터는 제공 안 함

**YouTube Analytics API (OAuth 필수)**
- 채널 소유자의 OAuth access_token이 있어야만 접근 가능
- 수익(estimatedRevenue), CPM, RPM, 시청시간, 노출수 등 민감한 데이터 제공
- 호출 즉시 응답 — Google이 이미 집계해둔 데이터를 쿼리하는 방식 (배치 대기 없음)

| | YouTube Data API | YouTube Analytics API |
|---|---|---|
| 인증 | API 키 | OAuth (채널 소유자 동의) |
| 구독자, 조회수 | O | O |
| 수익, CPM, RPM | X | O |
| 시청 시간, 노출 | X | O |

**기억 포인트:** Data API = 공개 지표, Analytics API = 수익/성과 지표. 후자는 채널 소유자 OAuth 없이 절대 불가.

---

### Q. 크리에이터가 OAuth 동의를 한 이후에는 어떻게 데이터를 가져오나요?

**A.** 크리에이터가 한 번만 동의하면, 이후 서버가 자동으로 처리.

1. 크리에이터가 OAuth 동의 → access_token + refresh_token 발급
2. refresh_token을 DB에 암호화 저장
3. 이후 서버가 access_token 만료 5분 전에 자동 갱신 (스케줄러)
4. 크리에이터는 재로그인 또는 추가 행동 불필요

**토큰 유효기간:**
- access_token: 1시간
- refresh_token: 장기 유효 (사용자가 앱 접근 권한 취소 시 만료)

**관련 프로젝트 코드:** `YouTubeAnalyticsClient.java`, `users.google_oauth_refresh_token` 컬럼

**기억 포인트:** refresh_token이 핵심. 이걸 DB에 안전하게 보관해야 크리에이터가 다시 동의하러 올 필요가 없음.

---
