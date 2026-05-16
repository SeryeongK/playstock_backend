# Spring 학습 노트

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-05-13

### Q. application.yml vs application.properties 차이는?

**A.** 같은 Spring Boot 설정 파일이지만 형식만 다름.

```properties
# application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/finnect
spring.datasource.username=postgres
```

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finnect
    username: postgres
```

- `.yml`: 들여쓰기로 계층 구조 표현. 가독성 좋고 현재 업계 표준
- `.properties`: flat key=value. 간단하지만 중첩 설정이 길어짐
- 두 파일 동시 존재 시 `.properties`가 우선 적용 → **혼용 금지**

**기억 포인트:** 새 프로젝트는 `.yml`로 통일. 두 파일 동시에 두지 말 것.

---

### Q. ddl-auto 옵션들의 차이는?

**A.** JPA가 앱 시작 시 DB 스키마를 어떻게 처리할지 결정하는 옵션.

| 옵션 | 동작 |
|---|---|
| `create` | 시작 시 테이블 DROP 후 재생성 (데이터 날아감) |
| `create-drop` | 시작 시 생성, 종료 시 DROP |
| `update` | 변경된 컬럼만 추가 (컬럼 삭제는 안 함) |
| `validate` | 엔티티와 DB 스키마 일치 여부만 검증, 불일치 시 앱 기동 실패 |
| `none` | 아무것도 안 함 |

- **운영 환경**: `validate` 또는 `none` — Flyway/Liquibase로 마이그레이션 관리
- **로컬 개발**: `update` (데이터 유지하며 스키마 변경) 또는 `create` (매번 초기화)

**기억 포인트:** 운영에 `create` 쓰면 데이터 전부 날아감. 핀넥트 운영은 `validate`.

---

### Q. @RestControllerAdvice가 뭐야?

**A.** 모든 `@RestController`에서 발생하는 예외를 한 곳에서 처리하는 전역 예외 핸들러.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PlaystockException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlaystockException(PlaystockException e) {
        return ResponseEntity
            .status(e.getErrorCode().getStatus())
            .body(ApiResponse.failure(e.getErrorCode()));
    }
}
```

- `@ControllerAdvice` + `@ResponseBody`의 합성 어노테이션
- `@ExceptionHandler(특정예외.class)` 메서드로 예외별 처리 분기

**기억 포인트:** 예외는 던지기만 하면 됨. 잡는 건 여기서 전부 처리.

---

### Q. ResponseEntity가 뭐야?

**A.** HTTP 응답 전체(상태코드 + 헤더 + body)를 직접 제어하는 래퍼 클래스.

```java
// 상태코드를 직접 지정
ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(...))

// 자주 쓰는 축약형
ResponseEntity.ok(body)                    // 200
ResponseEntity.badRequest().body(...)      // 400
ResponseEntity.internalServerError().body(...) // 500
```

- 단순히 객체를 반환하면 기본 200. 4xx/5xx 응답이 필요할 때 `ResponseEntity` 사용
- `GlobalExceptionHandler`에서 주로 활용

**기억 포인트:** 에러 응답은 GlobalExceptionHandler에서 `ResponseEntity`로 상태코드 지정.

---

### Q. Spring AI BOM(Bill of Materials)이 뭐야?

**A.** 여러 라이브러리의 호환되는 버전 조합을 일괄 관리하는 POM.

```kotlin
// build.gradle.kts
dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.0")
    }
}

dependencies {
    // BOM 선언 후 버전 생략 가능 — BOM이 호환 버전 보장
    implementation("org.springframework.ai:spring-ai-starter-model-anthropic")
}
```

- BOM 없이 쓰면 spring-ai 모듈들 간 버전 불일치로 런타임 에러 날 수 있음
- Spring Boot도 자체 BOM 내장 (그래서 `spring-boot-starter-*`는 버전 안 씀)

**기억 포인트:** Spring AI 쓸 때 BOM 먼저 선언, 이후 의존성은 버전 생략.

---

## 2026-05-14

### Q. Spring Boot는 .env 파일을 자동으로 읽지 않는데, 로컬 환경변수를 어떻게 주입하는가?

**A.** Spring Boot 외부화 설정 우선순위: OS 환경변수 > JVM 시스템 프로퍼티 > application.yml.
`.env`는 표준이 아니므로 직접 OS에 export되어야 Spring이 읽는다.

**방법 4가지:**

1. **spring-dotenv 라이브러리** — `.env`를 Spring Environment에 자동 등록. 코드 변경 없이 가장 깔끔.
   ```kotlin
   // build.gradle.kts
   implementation("me.paulschwarz:spring-dotenv:4.0.0")
   ```

2. **쉘에서 export 후 실행** — 라이브러리 의존성 없음. 터미널 세션 종료 시 사라짐.
   ```bash
   set -a; source .env; set +a; ./gradlew bootRun
   ```

3. **IntelliJ Run Configuration** — Edit Configurations → Environment variables에 직접 입력 또는 EnvFile 플러그인으로 .env 파일 연결.

4. **application-local.yml** — 값 직접 작성 후 `.gitignore`에 추가. Profile로 분리.
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

**기억 포인트:** `.env`는 OS에 export되어야 Spring이 읽는다. `spring-dotenv`가 그 과정을 자동화해줌.

---

### Q. spring.jpa.open-in-view가 뭐야?

**A.** HTTP 요청 전체 생명주기 동안 DB 커넥션을 열어두는 옵션. 기본값 `true`.

**문제점:**
- Controller/View에서 Lazy Loading이 가능해짐 → Service 레이어 밖에서 DB 쿼리가 실행되는 안티패턴 허용
- 커넥션 풀을 오래 점유 → 트래픽 많을 때 커넥션 부족

**false로 설정하면:**
- Service 레이어 트랜잭션 안에서만 DB 접근 강제
- Controller에서 Lazy Loading 시 `LazyInitializationException` 발생 → 실수 런타임에 빠르게 발견

**기억 포인트:** REST API 서버는 View가 없으므로 `false`가 맞다. `true`는 Thymeleaf 같은 서버사이드 렌더링용 레거시 설정.

---

### Q. SpringDoc OpenAPI(Swagger) 어노테이션은 어떻게 작동해?

**A.** 앱 시작 시 SpringDoc이 `@RestController`를 스캔해서 OpenAPI JSON을 자동 생성. Swagger UI가 그 JSON을 시각화.

**주요 어노테이션:**

| 어노테이션 | 위치 | 역할 |
|---|---|---|
| `@Tag` | 클래스 | API 그룹 이름 (사이드바 카테고리) |
| `@Operation` | 메서드 | 엔드포인트 요약 설명 |
| `@Parameter` | 파라미터 | 파라미터 설명 |
| `@ApiResponse` | 메서드 | 응답 코드별 설명 |
| `@Schema` | DTO 필드 | 필드 설명, 예시값 |

**동작 원리:**
1. 앱 시작 시 SpringDoc이 `@RestController` 클래스 스캔
2. 어노테이션 + 메서드 시그니처로 OpenAPI JSON 자동 생성
3. `/api-docs`로 JSON 노출 → Swagger UI(`/swagger-ui.html`)가 렌더링

**어노테이션 없어도 자동 감지되는 것:**
- URL, HTTP 메서드 (`@GetMapping`, `@PostMapping` 등)
- 요청/응답 바디 타입 (제네릭 포함)
- `@Valid` + `@NotBlank` 등 유효성 규칙

**기억 포인트:** 어노테이션은 "설명 추가"용. 없어도 기본 문서는 자동 생성됨.
