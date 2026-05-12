---
description: 현재 브랜치 변경사항 기반으로 PR 본문 작성
argument-hint: [base 브랜치, 기본값: main]
---

# PR 본문 작성

`pr-writer` 서브에이전트를 호출하여 현재 브랜치의 PR 본문을 작성하라.

## Base 브랜치
$ARGUMENTS (없으면 `main` 사용. 프로젝트가 `develop` 쓰면 그것)

## 자동 수행
1. `git status` 및 변경 사항 확인
2. 커밋 히스토리 분석
3. 관련 ADR 자동 검색
4. 테스트 실행 결과 확인 (`./gradlew test`)
5. 마크다운 PR 본문 생성

## 출력
GitHub에 그대로 복붙 가능한 마크다운 형식.

PR 제목도 같이 제안 (컨벤션: `[feat|fix|refactor|chore|docs|test] 한 줄 요약`).
