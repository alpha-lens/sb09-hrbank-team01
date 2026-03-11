# 코드 분석 및 수정 보고서

## 분석 일자: 2026-03-11
## 분석 대상: file-management 브랜치 전체 소스코드

---

## 1. 발견된 오류 및 수정 내역

### 1-1. build.gradle — 존재하지 않는 테스트 의존성 (심각)

**문제:** 37~39라인의 테스트 의존성이 실제로 존재하지 않는 아티팩트였음
```gradle
# 수정 전 (오류)
testImplementation 'org.springframework.boot:spring-boot-starter-data-jpa-test'
testImplementation 'org.springframework.boot:spring-boot-starter-validation-test'
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'
```
```gradle
# 수정 후
testImplementation 'org.springframework.boot:spring-boot-starter-test'
```

**영향:** 빌드 실패 — Gradle이 의존성을 해석할 수 없어 컴파일 자체가 불가능

---

### 1-2. schema.sql — 인덱스 생성문에 IF NOT EXISTS 누락 (중간)

**문제:** `employee_histories`와 `backups` 테이블의 인덱스 생성문에 `IF NOT EXISTS`가 빠져있었음

```sql
# 수정 전
CREATE INDEX idx_history_employee_number ON employee_histories (employee_number);
CREATE INDEX idx_backup_status           ON backups (status);

# 수정 후
CREATE INDEX IF NOT EXISTS idx_history_employee_number ON employee_histories (employee_number);
CREATE INDEX IF NOT EXISTS idx_backup_status           ON backups (status);
```

**영향:** 스키마 초기화 스크립트를 재실행하면 "index already exists" 오류 발생

---

### 1-3. application-product.yml — 파일이 비어있음 (심각)

**문제:** 운영(product) 프로파일 설정 파일이 완전히 비어있어, `--spring.profiles.active=product`로 실행하면 DB 연결 실패

**수정:** PostgreSQL 연결 설정 추가
- 환경변수 기반 DB 접속 정보 (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`)
- `ddl-auto: validate` (운영 환경에서는 스키마 자동 변경 방지)
- 파일 업로드 제한 설정 (10MB/30MB)

---

### 1-4. BinaryContentController — API 엔드포인트 불완전 (중간)

**문제:** 파일 다운로드(GET)만 구현되어 있고, 서비스 계층에 정의된 3개 기능이 컨트롤러에서 사용되지 않았음

**추가된 엔드포인트:**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/files` | 파일 업로드 → 201 Created + 메타데이터 반환 |
| `GET` | `/api/files/{id}` | 파일 메타데이터 조회 |
| `DELETE` | `/api/files/{id}` | 파일 삭제 (DB + 디스크) → 204 No Content |

기존 `GET /api/files/{id}/download` (파일 다운로드)는 그대로 유지

---

### 1-5. GlobalExceptionHandler — 파일 크기 초과 예외 미처리 (경미)

**문제:** `MaxUploadSizeExceededException` 핸들러가 없어서, 10MB 초과 파일 업로드 시 Spring 기본 에러 페이지가 반환됨

**수정:** 전용 핸들러 추가 → 400 Bad Request + 한국어 메시지 반환

---

### 1-6. 리소스 미존재 시 HTTP 상태코드 부적절 (중간)

**문제:** 파일이 존재하지 않을 때 `IllegalArgumentException` → 400 Bad Request로 반환되었음. 의미상 404 Not Found가 적절함.

**수정:**
- `ResourceNotFoundException` 커스텀 예외 클래스 생성
- `BinaryContentServiceImpl`에서 파일 미존재 시 `ResourceNotFoundException` 사용
- `GlobalExceptionHandler`에 404 핸들러 추가

---

## 2. 테스트 결과

### 2-1. 빌드 테스트
```
BUILD SUCCESSFUL in 5s
7 actionable tasks: 5 executed, 2 up-to-date
```

### 2-2. API 동작 테스트

| # | 테스트 항목 | 기대 상태 | 실제 상태 | 결과 |
|---|-----------|----------|----------|------|
| 1 | `POST /api/files` — 파일 업로드 | 201 | 201 | PASS |
| 2 | `GET /api/files/1` — 메타데이터 조회 | 200 | 200 | PASS |
| 3 | `GET /api/files/1/download` — 파일 다운로드 | 200 | 200 | PASS |
| 4 | `DELETE /api/files/1` — 파일 삭제 | 204 | 204 | PASS |
| 5 | `GET /api/files/1` — 삭제된 파일 조회 | 404 | 404 | PASS |
| 6 | `GET /api/files/9999` — 존재하지 않는 파일 조회 | 404 | 404 | PASS |
| 7 | `POST /api/files` — 빈 파일 업로드 | 400 | 400 | PASS |
| 8 | Swagger UI 접근 (`/swagger-ui.html`) | 302 (리다이렉트) | 302 | PASS |
| 9 | H2 Console 접근 (`/h2-console`) | 302 (리다이렉트) | 302 | PASS |

### 2-3. 응답 본문 검증

**파일 업로드 응답:**
```json
{"id":1,"fileName":"test2.txt","size":20,"contentType":"text/plain"}
```

**파일 다운로드:** 업로드한 원본 내용과 동일하게 반환됨

**삭제 후 조회 응답:**
```json
{"message":"파일을 찾을 수 없습니다. id=1"}
```

**빈 파일 업로드 응답:**
```json
{"message":"업로드된 파일이 없습니다."}
```

---

## 3. 정상 확인된 코드

| 파일 | 상태 |
|------|------|
| `HrbankApplication.java` | 정상 |
| `BaseEntity.java` | 정상 — JPA Auditing으로 `createdAt` 자동 관리 |
| `BaseUpdatableEntity.java` | 정상 — `updatedAt` 자동 관리 |
| `BinaryContent.java` | 정상 — Builder 패턴, 경로 업데이트 메서드 |
| `BinaryContentDto.java` | 정상 — Java Record 사용 |
| `BinaryContentRepository.java` | 정상 |
| `BinaryContentService.java` | 정상 — 인터페이스 설계 적절 |
| `BinaryContentServiceImpl.java` | 정상 — 트랜잭션 관리, 2단계 저장 로직 |
| `FileStorageService.java` | 정상 — 파일 CRUD + 디렉토리 자동 생성 |
| `WebConfig.java` | 정상 — CORS 설정 |
| `JpaConfig.java` | 정상 — JPA Auditing 활성화 |
| `application.yaml` | 정상 |
| `application-local.yml` | 정상 |

---

## 4. 수정된 파일 목록

| 파일 | 변경 유형 |
|------|----------|
| `build.gradle` | 수정 — 테스트 의존성 교체 |
| `src/main/resources/schema.sql` | 수정 — IF NOT EXISTS 추가 |
| `src/main/resources/application-product.yml` | 신규 — PostgreSQL 설정 추가 |
| `src/main/java/.../controller/BinaryContentController.java` | 수정 — 3개 엔드포인트 추가 |
| `src/main/java/.../global/GlobalExceptionHandler.java` | 수정 — 404, 파일크기 초과 핸들러 추가 |
| `src/main/java/.../global/ResourceNotFoundException.java` | 신규 — 커스텀 예외 클래스 |
| `src/main/java/.../service/BinaryContentServiceImpl.java` | 수정 — ResourceNotFoundException 적용 |
| `gradle/wrapper/gradle-wrapper.jar` | 복구 — 누락된 wrapper JAR 다운로드 |

---

## 5. 향후 주의사항

### 5-1. schema.sql과 Entity 클래스 불일치
`schema.sql`에 정의된 5개 테이블 중 `binary_contents`만 JPA Entity로 구현됨.
나머지 테이블에 대한 Entity 클래스가 구현되면 schema.sql과 컬럼명/타입이 일치하는지 검증 필요:
- `departments` → Department 엔티티 필요
- `employees` → Employee 엔티티 필요
- `employee_histories` → EmployeeHistory 엔티티 필요
- `backups` → Backup 엔티티 필요

### 5-2. Spring Boot 버전 확인
`build.gradle`에 Spring Boot 4.0.3이 설정되어 있음. 해당 버전의 `spring-boot-h2console` 모듈 호환성을 확인할 것.
