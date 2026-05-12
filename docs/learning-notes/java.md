# Java 학습 노트

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-05-13

### Q. Java record가 뭐야?

**A.** Java 16+에서 추가된 불변 데이터 클래스 선언 문법. 생성자, getter, equals, hashCode, toString을 자동 생성.

```java
// 이 한 줄이
public record ErrorDetail(String code, String message) {}

// 아래 전부와 동일
// - private final String code;
// - private final String message;
// - 전체 인자 생성자
// - code(), message() getter
// - equals(), hashCode(), toString() 자동 생성
```

- DTO처럼 데이터만 담는 클래스에 적합
- Lombok `@Value`와 유사하지만 언어 내장 기능 (의존성 불필요)
- 상속 불가, 필드 추가 불가 (불변성 보장)

**관련 프로젝트 코드:** `ApiResponse.java`의 `ErrorDetail` 내부 record

**기억 포인트:** 불변 DTO = record. Lombok 없이 깔끔하게.

---

### Q. @JsonInclude(JsonInclude.Include.ALWAYS)가 왜 필요해?

**A.** Jackson이 기본적으로 `null` 필드를 직렬화할 때 생략할 수 있어서, `"error": null`이 응답 JSON에서 빠질 수 있음.

```java
@JsonInclude(JsonInclude.Include.ALWAYS)
public record ApiResponse<T>(T data, ErrorDetail error) {
    // ALWAYS 없으면 성공 시 "error" 키 자체가 응답에서 사라질 수 있음
    // ALWAYS 있으면 → { "data": {...}, "error": null } 항상 보장
}
```

| 옵션 | 동작 |
|---|---|
| `ALWAYS` | null이어도 항상 포함 |
| `NON_NULL` | null 필드는 응답에서 제거 |
| `NON_EMPTY` | null + 빈 컬렉션/문자열 제거 |

**기억 포인트:** API 응답 구조 `{ "data": ..., "error": null }` 고정 포맷 유지하려면 `ALWAYS` 필수.

---

### Q. ErrorCode enum에 HttpStatus를 함께 담는 이유는?

**A.** 예외 하나에 관련 정보(HTTP 상태코드, 에러 코드, 메시지)를 묶어두면 GlobalExceptionHandler에서 분기 로직이 필요 없음.

```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"),
    INSUFFICIENT_SHARES(HttpStatus.BAD_REQUEST, "INSUFFICIENT_SHARES", "잔여 조각이 부족합니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
```

```java
// 서비스에서 그냥 던지기만 하면
throw new PlaystockException(ErrorCode.CHANNEL_NOT_FOUND);

// GlobalExceptionHandler가 자동으로 404 + 메시지 반환
// → { "data": null, "error": { "code": "CHANNEL_NOT_FOUND", "message": "채널을 찾을 수 없습니다" } }
```

- 새 에러 추가 시 enum 한 줄만 추가하면 됨 (Handler 수정 불필요)

**관련 프로젝트 코드:** `ErrorCode.java`, `GlobalExceptionHandler.java`

**기억 포인트:** 에러 정보는 enum에 집중. 흩어지면 유지보수 지옥.
