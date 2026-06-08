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

---

## 2026-05-17

### Q. @ManyToOne이 뭐야? 모델 간 관계를 정의하는 거지?

**A.** 맞음. 엔티티 간 관계를 Java 객체로 표현하는 어노테이션. DB의 FK 관계를 매핑.

```java
// Channel 엔티티 안에서
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "creator_id")  // DB 컬럼명
private User creator;
```

- DB: `channels.creator_id` → `users.id` FK
- Java: 채널 하나에 크리에이터(유저) 하나 → Many(채널) To One(유저)
- `@JoinColumn(name = "creator_id")`: 매핑할 FK 컬럼명 지정

**관계 어노테이션 종류:**

| 어노테이션 | 의미 | 예시 |
|---|---|---|
| `@ManyToOne` | N:1 | 채널 → 크리에이터 |
| `@OneToMany` | 1:N | 크리에이터 → 채널 목록 |
| `@OneToOne` | 1:1 | 유저 → 프로필 |
| `@ManyToMany` | N:M | 보통 중간 테이블로 분리 |

**기억 포인트:** `@ManyToOne`은 FK 있는 쪽(자식)에 선언. "Many가 One을 가리킨다."

---

### Q. FetchType.LAZY와 EAGER의 차이는? 왜 LAZY를 써?

**A.** 연관된 엔티티를 언제 DB에서 가져오는지의 차이.

```
EAGER: Channel 조회 → creator(User)도 즉시 JOIN해서 같이 조회
LAZY:  Channel 조회 → creator는 일단 안 가져옴
                       creator.getNickname() 호출 시점에 그때 DB 조회
```

**LAZY를 기본으로 쓰는 이유:**
- 채널 100개 조회 시 creator 정보가 필요 없으면, EAGER는 쓸모없는 JOIN 쿼리를 항상 실행
- 필요할 때만 가져오는 게 성능상 유리

**LAZY의 단점 — N+1 문제:**
```java
List<Channel> channels = channelRepository.findAll(); // 쿼리 1번
for (Channel c : channels) {
    c.getCreator().getNickname(); // creator 조회 쿼리가 채널 수만큼 추가 발생
}
// 채널 100개면 총 101번 쿼리 → N+1 문제
```

**해결법:** `@EntityGraph` 또는 JPQL `JOIN FETCH`로 필요한 경우에만 한 번에 가져옴.

**기억 포인트:** LAZY가 기본 원칙. 목록 조회처럼 연관 엔티티가 반복 필요한 경우에만 JOIN FETCH로 최적화.

---

### Q. Repository는 어떤 역할이야? 공통 Repository는 안 써?

**A.** "특정 테이블 전담 DB 도구". 엔티티 하나 = Repository 하나가 JPA 설계 원칙.

- 공통 Repository는 거의 안 씀 — 어떤 테이블을 다루는지 불명확해지기 때문
- Util과의 차이: Util은 범용·DB 없음 / Repository는 특정 테이블·DB 전담

---

### Q. JPA Repository 쿼리 메서드 이름 규칙이 뭐야?

**A.** `동사 + By + 필드명` 조합. Spring Data JPA가 이름을 파싱해서 쿼리 자동 생성.

```java
// 기본
findByEmail(String email)               // WHERE email = ?
existsByEmail(String email)             // COUNT(*) > 0
countByStatus(ChannelStatus status)     // COUNT(*)

// 조건 조합
findByCreatorIdAndStatus(Long creatorId, ChannelStatus status)  // AND
findByStatusOrTier(ChannelStatus s, ChannelTier t)              // OR

// 비교
findByPriceGreaterThan(Integer price)   // price > ?
findByPriceLessThanEqual(Integer price) // price <= ?

// 정렬
findByStatusOrderByCreatedAtDesc(ChannelStatus status)

// LIKE
findByNameContaining(String keyword)    // LIKE '%keyword%'
```

복잡한 쿼리는 이름이 너무 길어지면 `@Query`로 JPQL 직접 작성:
```java
@Query("SELECT c FROM Channel c WHERE c.status = :status AND c.dividendRate > :rate")
List<Channel> findActiveHighYield(@Param("status") ChannelStatus status, @Param("rate") BigDecimal rate);
```

---

### Q. findAByB (Projection) vs findByB + DTO 변환, 뭐가 다르고 뭘 써야 해?

**A.** 둘 다 특정 필드만 반환하는 방법. 실무에서는 후자(DTO 변환) 선호.

**Projection 방식:**
```java
public interface ChannelSummary {
    String getName();
    Integer getPrice();
}
List<ChannelSummary> findNameAndPriceByStatus(ChannelStatus status);
// name, price만 SELECT
```

**일반 방식 (DTO 변환):**
```java
List<Channel> findByStatus(ChannelStatus status);  // 엔티티 전체 조회

// Response DTO에서 필요한 필드만 골라 담기
public static ChannelResponse from(Channel channel) {
    return new ChannelResponse(channel.getId(), channel.getName(), channel.getPrice());
}
```

**후자를 쓰는 이유:**
- Projection 인터페이스가 늘어나면 관리 복잡
- DTO 변환 로직을 `Response.from(entity)` 한 곳에 집중 가능
- 핀넥트 규칙(`~Response` DTO 통일)과 일치

**기억 포인트:** 쿼리 메서드는 `findByB`로 엔티티 조회 → `Response.from(entity)`로 변환. Projection은 성능 최적화가 필요한 극단적 상황에서만.
