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
