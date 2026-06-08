# Testing 학습 노트

> PHP → Java 전환 학습 기록 / 핀넥트 프로젝트 중 정리

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-06-08

### Q. assertThatThrownBy 체인이 각각 무슨 의미야?

**A.**

```java
assertThatThrownBy(() -> signupService.signup(request))  // 1단계
        .isInstanceOf(PlaystockException.class)           // 2단계
        .satisfies(ex -> assertThat(((PlaystockException) ex).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_EMAIL));   // 3단계
```

- **1단계**: "이 코드를 실행했을 때 예외가 던져지는지 봐줘". 람다로 감싸는 이유는 지금 바로 실행하지 않고 assertThat에게 실행을 맡기기 위해서.
- **2단계 `.isInstanceOf(...)`**: 던져진 예외가 해당 타입인지 확인.
- **3단계 `.satisfies(ex -> ...)`**: 예외 객체를 꺼내서 내부 필드(errorCode 등)를 추가 검증할 때 사용.

메시지만 확인할 때는 더 단순한 대안:
```java
assertThatThrownBy(() -> signupService.signup(request))
        .isInstanceOf(PlaystockException.class)
        .hasMessage("이미 사용 중인 이메일입니다");
```

**기억 포인트:** errorCode 자체를 검증하는 게 더 정확하므로 `satisfies` 방식 권장. 메시지는 바뀔 수 있지만 코드는 바뀌지 않음.

**관련 프로젝트:** `SignupServiceTest.java`

---

### Q. @Mock, @InjectMocks, given().willReturn() 패턴이 뭐야?

**A.**

```java
@ExtendWith(MockitoExtension.class)
class SignupServiceTest {

    @Mock
    private UserRepository userRepository;  // 가짜 객체 생성

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SignupService signupService;  // @Mock들을 자동 주입해서 진짜 객체 생성
}
```

- **`@Mock`**: 실제 DB 연결 없이 동작하는 가짜 객체. 메서드 호출은 되지만 아무것도 안 함.
- **`@InjectMocks`**: 테스트 대상 클래스. `@Mock`으로 만든 가짜 객체들을 생성자 주입으로 꽂아줌.
- **`given(...).willReturn(...)`**: "이 메서드가 호출되면 이걸 반환해라" 선언 (BDD 스타일).

```java
// Mock 행동 정의
given(userRepository.existsByEmail("test@example.com")).willReturn(false);
given(passwordEncoder.encode("password123")).willReturn("hashed_password");

// 특정 메서드 호출 여부 검증
verify(userRepository).save(any(User.class));
verify(userRepository, never()).save(any());  // 절대 호출 안 됐어야 함
```

**기억 포인트:** `@Mock`은 가짜 객체, `@InjectMocks`는 실제 테스트 대상. 둘 다 선언해야 연결됨.

**관련 프로젝트:** `SignupServiceTest.java`

---

### Q. SignupRequest에 setter가 없는데 테스트에서 어떻게 값을 넣어?

**A.** `ReflectionTestUtils.setField()`로 리플렉션을 통해 private 필드에 직접 값을 주입한다.

```java
SignupRequest request = new SignupRequest();
ReflectionTestUtils.setField(request, "email", "test@example.com");
ReflectionTestUtils.setField(request, "nickname", "테스터");
ReflectionTestUtils.setField(request, "password", "password123");
ReflectionTestUtils.setField(request, "role", UserRole.INVESTOR);
```

`spring-boot-starter-test`에 포함되어 있어 별도 의존성 추가 불필요.

**기억 포인트:** setter 없는 DTO에 값 넣을 때 `ReflectionTestUtils.setField()`. 테스트 코드 전용 우회 수단.

**관련 프로젝트:** `SignupServiceTest.java`

---
