# JPA 학습 노트

> PHP → Java 전환 학습 기록 / 핀넥트 프로젝트 중 정리

이 파일은 코딩 중 만난 개념과 질문을 정리한 노트입니다.
면접 대비 및 향후 복습용.

---

## 2026-05-16

### Q. @Entity, @Table, @Column, @Enumerated 어노테이션은 각각 뭐야?

**A.** JPA 엔티티 선언에 쓰는 핵심 어노테이션들.

| 어노테이션 | 역할 |
|---|---|
| `@Entity` | "이 클래스가 DB 테이블이다"라고 JPA에 선언. 관리 대상으로 인식. |
| `@Table(name = "users")` | 매핑할 테이블 이름 명시. 클래스명과 테이블명이 다를 때 사용. |
| `@Column(name = "password_hash")` | 필드와 매핑할 컬럼 이름 명시. `nullable`, `unique` 등 제약도 설정 가능. |
| `@Enumerated(EnumType.STRING)` | enum을 문자열로 저장. `ORDINAL`은 enum 순서 바뀌면 데이터 깨지므로 `STRING` 필수. |
| `@Id` | PK 필드 지정. |
| `@GeneratedValue(strategy = GenerationType.IDENTITY)` | DB의 auto increment(PostgreSQL: BIGSERIAL) 전략 사용. |

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole role;
}
```

**기억 포인트:** `@Enumerated`는 항상 `STRING`. `ORDINAL`은 enum 순서 변경 시 데이터 깨짐.

---

### Q. @PrePersist가 뭐야?

**A.** 엔티티가 DB에 INSERT되기 직전에 자동 호출되는 JPA 콜백 메서드.

```java
@PrePersist
protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    if (this.pointBalance == null) {
        this.pointBalance = 0L;
    }
}
```

- `created_at` 자동 설정, 기본값 초기화 등에 사용
- DB의 `DEFAULT` 값과 Java 객체를 동기화할 때 유용
- `@PreUpdate`: UPDATE 직전 호출 (예: `updated_at` 갱신)

**기억 포인트:** INSERT 직전 자동 실행. `createdAt` 자동화에 딱.

---

### Q. JpaRepository는 뭐야? 구현체를 직접 안 짜도 돼?

**A.** Spring Data JPA가 제공하는 인터페이스. 선언만 하면 런타임에 구현체를 자동 생성해줌.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // 기본 CRUD는 자동 제공: findById, save, delete, findAll ...

    // 메서드 이름 규칙으로 자동 쿼리 생성
    Optional<User> findByEmail(String email);
    // → SELECT * FROM users WHERE email = ?

    boolean existsByEmail(String email);
    // → SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // 객체 조회 없이 존재 여부만 확인 → 중복 체크에 적합
}
```

- 복잡한 쿼리는 `@Query`로 직접 작성
- `JpaRepository<엔티티, PK타입>` 형태로 제네릭 지정

**기억 포인트:** 인터페이스만 선언하면 CRUD 완성. 이름 규칙이 곧 쿼리.

---

### Q. Entity / Repository / Enum 세 파일이 세트인 이유는?

**A.** 의존 방향이 단방향으로 이어지기 때문에 함께 묶임.

```
UserRole (enum)
    ↑ 참조
User (Entity)
    ↑ 다루는 대상
UserRepository (Repository)
    ↑ 호출
UserService (Service)
```

호출 흐름:
`Service → UserRepository.findByEmail() → DB 조회 → User 객체 반환 (role 필드는 UserRole enum)`

- `UserRole`은 `User` 엔티티만 알면 됨
- `UserRepository`는 `User`만 알면 됨
- 역방향 참조 없음 → 세 파일이 하나의 응집 단위

**관련 프로젝트 코드:** `User.java`, `UserRole.java`, `UserRepository.java`

**기억 포인트:** enum → Entity → Repository 단방향. 역참조 없으면 세트로 묶어도 됨.
