---
description: 기능 단위 문서 관리 (start | update | done | reindex)
argument-hint: [start|update|done|reindex] [F번호 또는 제목]
---

# Feature Note 관리

`feature-note-keeper` 서브에이전트를 호출하여 기능 단위 문서를 관리하라.

## 명령
$ARGUMENTS

## 4가지 모드

### `start {제목}` — 새 기능 시작
- 다음 F번호 자동 부여
- 설계 인터뷰 (목적/입출력/핵심 흐름 3가지만)
- INDEX.md에 🟡 상태로 추가

### `update {F번호} [섹션]` — 진행 중 업데이트
- `git diff` 분석하여 구현 섹션 자동 채움
- 섹션 미지정 시 가장 미완성된 섹션 추정

### `done {F번호}` — 기능 완료
- 검증 섹션 (테스트 실행 결과)
- 후속 섹션 (한계, 미해결, 학습 포인트)
- 관련 ADR/learning-note 자동 링크
- INDEX.md 상태 🟢로 변경

### `reindex` — 인덱스 새로고침
- 모든 F*.md 스캔
- INDEX.md 재생성

## 출력
처리한 작업 요약 + 파일 경로.

## 페이스메이커 체크
완료(`done`) 시 마지막 체크:
> "코드 안 읽고 이 Feature Note만으로 기능 파악 가능한가요?"

아니라면 부족한 부분 짚어줄 것.
