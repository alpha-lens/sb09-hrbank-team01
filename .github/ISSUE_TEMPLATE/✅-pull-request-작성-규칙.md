---
name: "✅ Pull Request 작성 규칙"
about: Describe this issue template's purpose here.
title: ''
labels: ''
assignees: ''

---

# ✅ Pull Request 작성 규칙
모든 팀원은 아래 규칙을 준수해 PR을 작성해주세요.
---

## 1. PR 제목 규칙
---
[타입] 작업 내용 요약

📌 예시
[feat] 사용자 로그인 기능 추가
[fix] 메시지 중복 전송 오류 수정

## 2. PR 설명 템플릿
---
📌 작업 개요
- Employee 패키지 커스텀 예외 생성

✅ 완료한 작업
 - [x] Employee 패키지에서 사용할 예외 클래스 생성
 - [x]  GlobalExceptionHandler로 예외 처리 테스트
 - [x]  기능 테스트

🧪 테스트 결과
 - 로컬 테스트 완료
 - Postman 테스트 완료 등

⚠️ 기타 참고 사항
- 

# 📝 커밋 메시지 컨벤션
---
커밋 메시지는 다음 형식을 따릅니다:
<타입>(옵션): <커밋 내용 요약>

## 📌 예시
feat: 채널 생성 기능 추가
fix(user): 로그인 실패 시 예외 처리 추가

| 타입 | 설명 |
| --- | --- |
| feat | 새로운 기능 추가 |
| fix | 버그 수정 |
| docs | 문서 수정 (README 등) |
| style | 코드 스타일 변경 (포맷, 세미콜론 등) |
| refactor | 코드 리팩토링 (기능 변화 없음) |
| test | 테스트 코드 추가 또는 수정 |
| chore | 기타 변경 (빌드 설정, 패키지 등) |
| ci | CI 설정 변경 |
| build | 빌드 시스템 관련 변경 |
| --- | --- |
