---
feature: F001
title: Spring Boot 프로젝트 초기 셋업
status: 🟢 완료
started: 2026-05-14
finished: 2026-05-14
---

# F001: Spring Boot 프로젝트 초기 셋업

**상태:** 🟢 완료
**기간:** 2026-05-14 ~ 2026-05-14
**관련 화면:** 없음 (인프라/설정)
**관련 PR:** TBD
**관련 ADR:** TBD

---

## 1. 설계 (Why & What)

### 목적
핀넥트(Finnect) 백엔드의 기반 구조를 잡는다. 이후 모든 기능(회원가입, 채널 등록, 거래, 배당)이 이 스캐폴드 위에 올라가므로, 공통 응답 포맷·예외 처리·의존성 구성을 초기에 확정한다.

### 입력 / 출력
- 입력: 없음 (애플리케이션 부트스트랩)
- 출력: 실행 가능한 Spring Boot 앱, `/swagger-ui.html` 접근 가능

### 핵심 흐름
1. Gradle Kotlin DSL로 의존성 선언 (Web, JPA, PostgreSQL, Spring AI, Swagger, Lombok)
2. `application.yml`에서 환경변수로 DB·AI·포트 설정 주입
3. 공통 `ApiResponse<T>` 래퍼와 `GlobalExceptionHandler`로 응답 포맷 통일

### 주요 결정
- H2 없이 개발·운영 모두 PostgreSQL 사용 (CLAUDE.md 원칙)
- `ddl-auto: validate` 기본값, 로컬에서는 환경변수로 `create-drop` 오버라이드
- 시크릿(DB 비밀번호, Anthropic API 키)은 환경변수로만 주입

---

## 2. 구현 (How)

### 패키지
`com.playstock`

### 파일 목록
- `PlaystockBackendApplication.java` — `@SpringBootApplication` 진입점
- `common/ApiResponse.java` — 통일 응답 래퍼 (`data` / `error` 필드)
- `common/GlobalExceptionHandler.java` — `@RestControllerAdvice`, 3종 핸들러
- `common/exception/ErrorCode.java` — HTTP 상태 + 코드 + 메시지 묶음 enum
- `common/exception/PlaystockException.java` — 도메인 전용 unchecked 예외
- `src/main/resources/application.yml` — DB, JPA, Spring AI, Swagger 설정

### API
없음 (이 기능 자체는 엔드포인트 없음)

### DB 변경
없음 (스키마 정의 전)

### 의존성
- 선행 기능: 없음 (최초 셋업)
- 외부 API: Anthropic Claude (설정만, 호출 없음)
- 라이브러리: Spring Boot 3.5, Spring AI 1.0, SpringDoc OpenAPI 2.8.8, Lombok

### 핵심 코드 (스니펫)
```java
// ApiResponse.java — 성공/실패 응답을 한 포맷으로 강제
public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(data, null);
}
public static <T> ApiResponse<T> failure(String code, String message) {
    return new ApiResponse<>(null, new ErrorDetail(code, message));
}
```

---

## 3. 검증 (Verify)

### 테스트
- `PlaystockBackendApplicationTests`: 1개 케이스 (컨텍스트 로드 확인)

### 수동 검증
- [ ] `./gradlew bootRun` 정상 시작
- [ ] `http://localhost:8080/swagger-ui.html` 접근 가능
- [ ] 존재하지 않는 경로 호출 시 `{ data: null, error: {...} }` 형식으로 응답

### Swagger
- `/swagger-ui.html`, `/api-docs`

---

## 4. 후속 (Follow-up)

### 알려진 한계
- Flyway 미적용 상태. 첫 엔티티 추가 전에 설정해야 함.
- Spring Security 미포함. 회원가입 기능(F002) 시작 전 추가 필요.

### 미해결
- `ddl-auto` 로컬 환경변수 기본값이 `validate`여서 DB 스키마 없으면 기동 불가. 로컬 개발 가이드 필요.

### 학습 포인트
- [Spring Boot application.yml 환경변수 바인딩](../learning-notes/spring.md)
- [Gradle Kotlin DSL 기초](../learning-notes/build-deploy.md)

### 향후 개선
- Flyway 설정 추가 (V1__ 초기 마이그레이션) → [F002](./F002-Flyway-DB-마이그레이션.md) 완료
- Spring Security + JWT 추가 (회원가입 기능 전)
- JVM 옵션 설정 (`-Xmx400m -Xms200m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC`)
