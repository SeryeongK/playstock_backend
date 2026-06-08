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

---

## 2026-06-08

### Q. OncePerRequestFilter는 내가 만든 게 아니라 Spring이 이미 제공하는 클래스인가?

**A.** 맞다. `org.springframework.web.filter.OncePerRequestFilter`는 Spring Framework가 제공하는 추상 클래스다. `extends`(상속)하고 추상 메서드 `doFilterInternal()`만 구현하면 된다.

**"OncePerRequest(요청당 한 번)"인 이유:**
서블릿 환경에서는 하나의 HTTP 요청이 내부적으로 여러 번 디스패치될 수 있다 (forward, include, 에러 페이지 처리). 일반 `Filter`를 쓰면 같은 요청에 필터가 여러 번 실행될 수 있다. `OncePerRequestFilter`는 내부적으로 "이 요청에서 이미 실행했는지" 표시해두고 중복 실행을 막아준다.

**역할 분담:**

| 구분 | 누가 만든 것 | 역할 |
|---|---|---|
| `OncePerRequestFilter` | Spring | 중복 실행 방지 + 필터 뼈대 제공 (추상 클래스) |
| `doFilterInternal()` | 우리 | 실제 JWT 파싱·검증 로직 (추상 메서드 구현) |
| `filterChain.doFilter()` | 서블릿 | 다음 필터로 요청 넘기기 (우리가 호출) |

**실제 사용 패턴 (JwtAuthenticationFilter):**
```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // JWT 검증 로직
        filterChain.doFilter(request, response); // 반드시 호출해서 다음 필터로 넘길 것
    }
}
```

**기억 포인트:** `filterChain.doFilter()`를 호출하지 않으면 요청이 거기서 멈춘다. 인증 실패 시에도 예외 대신 통과시키고 다음 인가 단계에서 403 처리하게 두는 게 일반적 패턴.

---

### Q. JWT 필터 → 인가 체크 두 단계로 나뉘는 이유는?

**A.** 인증(Authentication)과 인가(Authorization)는 다른 개념이기 때문이다.
- **인증(Authentication):** "너 누구야?" → JWT 필터가 담당. 토큰을 파싱해서 신원을 파악하고 SecurityContext에 저장.
- **인가(Authorization):** "너 여기 들어가도 돼?" → Spring Security가 담당. SecurityContext에서 꺼낸 신원 정보로 권한 확인.

```
토큰 없음  →  [1단계: 그냥 통과]  →  [2단계: 인증된 사람 없음 → 401]
토큰 위조  →  [1단계: 그냥 통과]  →  [2단계: 인증된 사람 없음 → 401]
토큰 정상  →  [1단계: SecurityContext 세팅]  →  [2단계: 권한 확인 → 통과 or 403]
```

**1단계(JWT 필터)는 판단하지 않고 파악만 한다.** 토큰이 없거나 위조됐어도 예외를 던지지 않고 그냥 통과시키고 SecurityContext를 공란으로 둔다. 401/403 판단은 2단계가 한다.

**SecurityContext = 두 단계 사이의 공용 메모장.** 1단계가 여기 쓰고, 2단계가 여기서 읽는다.

```java
// JwtAuthenticationFilter - 1단계: 파악만, 판단 안 함
if (token != null && jwtUtil.validateToken(token)) {
    SecurityContextHolder.getContext().setAuthentication(authentication);
}
filterChain.doFilter(request, response); // 실패해도 그냥 통과
```

**기억 포인트:** 필터는 신원 파악만, 거부는 Security가 한다. 역할 분리.

---

### Q. Bean이 뭐야? 컨테이너가 왜 필요해?

**A.** Bean = Spring이 `new` 해주고 필요한 곳에 알아서 배달해주는 객체.

**컨테이너가 필요한 이유:**
- 유지보수를 위해 역할을 쪼갠다 (UserRepository는 DB만, PasswordEncoder는 해싱만)
- 쪼개면 클래스들이 서로를 필요로 하는 의존성이 생긴다
- 의존성 연결을 직접 하면 클래스 수백 개일 때 수백 줄의 수동 배선이 필요하다
- 컨테이너가 이 배선을 자동으로 해준다

```java
// Bean 없이 직접 연결 — 순서까지 맞춰야 함
JwtUtil jwtUtil = new JwtUtil(secret, expirationMs);
UserRepository userRepo = new UserRepository(dataSource);
PasswordEncoder encoder = new BCryptPasswordEncoder();
LoginService loginService = new LoginService(userRepo, encoder, jwtUtil);

// Bean 사용 시 — 선언만 하면 Spring이 알아서 주입
@Service
@RequiredArgsConstructor
public class LoginService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
}
```

**어노테이션 정리:**

| 어노테이션 | 의미 |
|---|---|
| `@Component` | Bean으로 등록 |
| `@Service` | Bean + 서비스 계층 (의미 부여) |
| `@Repository` | Bean + DB 계층 |
| `@Configuration` | Bean + 설정 클래스 |

**기억 포인트:** Bean = Spring 창고에 등록된 객체. 컨테이너 = 창고 관리자 + 자동 배달부.

---

### Q. 다른 언어(Node.js)에서는 DI를 어떻게 해?

**A.** Node.js(Express)는 `import`로 직접 가져다 쓴다. 컨테이너 개념이 없다.

```javascript
// Node.js - 직접 import
import { userRepository } from './userRepository.js';
import bcrypt from 'bcrypt';

export const signupService = {
    signup: async (request) => { ... }
};
```

Node.js는 **모듈 캐시가 컨테이너 역할**을 한다. `import`한 모듈은 Node가 내부적으로 캐싱해서 어디서 불러도 같은 객체(자동 싱글톤).

**차이점:**

| | Node.js | Spring |
|---|---|---|
| 연결 방식 | `import`로 직접 | 컨테이너가 주입 |
| 싱글톤 | 모듈 캐시가 자동으로 | 컨테이너가 보장 |
| 테스트 시 교체 | 까다로움 (직접 엮여있어서) | 쉬움 (주입 구조라 Mock 교체 간단) |

**기억 포인트:** Node의 단점은 테스트 시 드러난다. `signupService`가 `userRepository`를 직접 import했기 때문에 Mock 교체가 까다롭다. Spring은 주입 구조라 `@Mock`으로 쉽게 교체 가능.

---

### Q. GET /users/me 같은 단순 조회는 Service 없이 Controller에서 Repository 직접 써도 돼?

**A.** 기준은 **비즈니스 로직이 있느냐**다.

- `findById` 한 줄짜리 조회 → Service 불필요, Controller에서 직접 사용 가능
- Service가 필요해지는 경우: 조회 후 가공(포인트 환산, 등급 계산), 다른 도메인 데이터와 합산, 이벤트 발행

```java
@GetMapping("/me")
public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("인증된 유저가 DB에 없음"));
    return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
}
```

**기억 포인트:** 지금 분리하면 오버엔지니어링. 보유 조각 합산이나 수익률 계산이 붙으면 그때 `UserService`로 분리.
