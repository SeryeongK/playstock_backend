# Security 학습 노트

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-05-16

### Q. CSRF가 뭐야? REST API에서 왜 비활성화해?

**A.** CSRF(Cross-Site Request Forgery) — 사용자가 의도하지 않은 요청을 악성 사이트가 대신 보내는 공격.

**공격 흐름:**
1. 사용자가 은행 사이트에 로그인 (세션 쿠키 발급)
2. 악성 사이트 방문 → 숨겨진 폼이 은행 송금 API 자동 호출
3. 브라우저가 세션 쿠키를 **자동 첨부** → 은행은 정상 요청으로 착각

**핵심 원인:** 브라우저는 해당 도메인 쿠키를 모든 요청에 자동으로 붙임.

**CSRF 방어 방식 (세션 기반):**
- 서버가 고유 토큰 발급 → 폼에 숨김 필드로 포함
- 서버가 요청마다 토큰 검증 → 악성 사이트는 토큰을 몰라서 차단

**REST API에서 disable하는 이유:**
- JWT는 쿠키가 아닌 Authorization 헤더로 전송: `Authorization: Bearer {token}`
- 브라우저가 헤더를 자동 첨부하지 않음 → 악성 사이트가 헤더를 흉내낼 수 없음
- JWT 기반 Stateless API는 CSRF 공격 자체가 불가능 → 방어 불필요

**기억 포인트:** 쿠키 세션 = CSRF 필요. JWT 헤더 인증 = CSRF disable 안전.

---

### Q. SessionCreationPolicy.STATELESS로 설정하는 이유는?

**A.** Spring Security가 서버에 HTTP 세션을 생성하지 않도록 강제하는 설정.

**기본값(STATEFUL) 동작:**
- 로그인 성공 시 서버가 세션 생성 → 세션 ID를 쿠키로 클라이언트에 전달
- 이후 요청마다 쿠키의 세션 ID로 서버가 사용자 식별
- 세션 정보를 서버 메모리에 보관

**STATELESS 동작:**
- 서버가 세션을 일절 만들지 않음
- 요청마다 JWT를 파싱해서 사용자 식별 → 서버는 아무것도 기억 안 함

**STATELESS를 선택하는 이유:**
1. **스케일아웃 용이** — 세션이 서버 메모리에 없으므로 서버를 여러 대로 늘려도 문제 없음. 세션 공유 인프라(Redis 등) 불필요.
2. **CSRF 위험 제거** — 세션+쿠키가 없으니 CSRF 공격 자체가 성립 안 함.
3. **REST 원칙** — REST는 각 요청이 독립적이어야 함(Stateless). JWT가 이 원칙에 부합.

**기억 포인트:** JWT 쓰면 서버가 상태를 기억할 필요가 없다. STATELESS = "세션 만들지 마라".

---

### Q. JWT Secret은 어디서 가져와? 직접 만들어야 해?

**A.** 직접 생성해야 함. 규칙은 **32자 이상 랜덤 문자열**.

```bash
openssl rand -base64 32
# 출력 예시: K7gNU3sdo+OL0wNhqoVWhr3g6s1xYv72ol/pe/Unols=
```

- 로컬 개발: `.env`에 아무 문자열이나 32자 이상으로 설정
- 운영 배포: Render 환경변수에 위 명령으로 생성한 랜덤값 입력
- HS256 알고리즘은 256bit(32바이트) 이상 키 필요 → 32자 미만이면 jjwt가 예외 발생

**기억 포인트:** Secret은 절대 git에 올리면 안 됨. `.env`는 `.gitignore`에 추가.

---

### Q. JwtAuthenticationFilter 코드가 어떻게 동작해?

**A.** `OncePerRequestFilter`를 상속한 Spring Security 필터. 매 요청마다 JWT를 검증하고 인증 정보를 등록.

**단계별 동작:**

```java
// 1. 토큰 유효성 검사
if (token != null && jwtUtil.validateToken(token)) {

    // 2. 토큰에서 사용자 정보 추출
    Long userId = jwtUtil.getUserId(token);
    UserRole role = jwtUtil.getRole(token);

    // 3. Spring Security에 인증 객체 등록
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            userId,   // principal — 나중에 Controller에서 꺼낼 수 있는 사용자 식별자
            null,     // credentials — JWT 방식이라 비밀번호 불필요
            List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))  // 권한
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
}

// 4. 반드시 다음 필터로 넘김 (토큰 유효 여부 무관)
filterChain.doFilter(request, response);
```

**핵심 개념:**

- **`SecurityContextHolder`**: Spring Security가 인증 정보를 보관하는 스레드 로컬 저장소. 여기에 인증 객체가 있어야 `anyRequest().authenticated()` 통과.
- **`UsernamePasswordAuthenticationToken`**: Spring Security의 인증 객체. 3인자 생성자(principal, credentials, authorities)를 쓰면 "인증 완료" 상태로 생성됨.
- **`filterChain.doFilter()`**: 이 줄이 없으면 요청이 필터에서 멈춰 Controller까지 못 감. 토큰 유무와 관계없이 항상 호출해야 함.
- **`OncePerRequestFilter`**: 같은 요청에서 필터가 중복 실행되지 않도록 보장하는 베이스 클래스.

**기억 포인트:** SecurityContextHolder가 비어있으면 Spring Security가 403 반환. 필터는 "채워주는 역할"만 하고, 판단은 SecurityConfig가 함.
