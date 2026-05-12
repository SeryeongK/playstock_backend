# 빌드 & 배포 학습 노트

## 2026-05-12

### Q. build.gradle이 무슨 역할이야?

**A.** Gradle 빌드 도구의 설정 파일. 의존성 선언, 빌드 방법, Java 버전을 정의한다.

주요 역할:
- **의존성 관리** — 필요한 라이브러리 선언 시 Maven Central에서 자동 다운로드
- **빌드 방법 정의** — 컴파일, 테스트, 패키징(JAR/WAR) 방법 정의
- **플러그인 적용** — `java`, `spring-boot` 플러그인으로 빌드 기능 확장
- **Java 버전 지정** — 컴파일 대상 Java 버전 설정

**`.kts` vs `.gradle`:** `.gradle` = Groovy DSL / `.gradle.kts` = Kotlin DSL (현재 권장, 타입 안전)

**기억 포인트:** `composer.json` = `build.gradle.kts`. `./gradlew build` 한 방으로 의존성 + 컴파일 + JAR 생성까지.

---

## 2026-05-13

### Q. Gradle toolchain이 뭐야? sourceCompatibility와 차이는?

**A.** 툴체인은 빌드에 사용할 JDK를 Gradle이 직접 관리하는 방식.

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

| 방식 | 동작 |
|---|---|
| `toolchain` | JDK 자체를 지정. Gradle이 JDK를 찾거나 자동 다운로드 |
| `sourceCompatibility` | 컴파일 대상 버전만 지정. 실행 JVM은 개발자가 별도 관리 |

- 툴체인 캐시 위치: `~/.gradle/jdks/`

**기억 포인트:** 팀원마다 JDK 버전 다를 때 toolchain 쓰면 Gradle이 알아서 맞춰줌.

---

### Q. gradle.properties는 뭐야?

**A.** Gradle 빌드 전체에 적용되는 설정 파일. `build.gradle.kts`가 "무엇을 빌드할지"라면, `gradle.properties`는 "어떻게 빌드할지" 환경 설정.

- `org.gradle.jvmargs=-Xmx2g` : Gradle 데몬 JVM 힙 크기
- `org.gradle.java.installations.paths=/path/to/jdk21` : 툴체인이 JDK를 못 찾을 때 수동 경로 등록
- `org.gradle.caching=true` : 빌드 캐시 활성화

**기억 포인트:** JDK 경로 문제 생기면 `gradle.properties`에 `installations.paths` 추가.

---

### Q. Gradle wrapper 버전 업그레이드 방법은?

**A.** `gradle/wrapper/gradle-wrapper.properties`의 `distributionUrl` 버전 번호만 변경. 다음 `./gradlew` 실행 시 자동 다운로드.

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.14-bin.zip
```

- 최신 버전 확인: `curl -s https://services.gradle.org/versions/current`
- Gradle 버전과 번들 Kotlin 버전은 세트 (예: Gradle 8.x → Kotlin DSL 버전도 함께 변경)

**기억 포인트:** wrapper 파일 한 줄만 바꾸면 팀 전체 Gradle 버전 동기화됨.
