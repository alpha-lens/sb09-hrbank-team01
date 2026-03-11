# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

HR Bank — 인사 관리 시스템 백엔드 API 서버.
- **Framework**: Spring Boot 4.0.3 / Java 17
- **Base package**: `com.team1.hrbank`
- **Build**: Gradle (Groovy DSL)

## 주요 명령어

```bash
./gradlew build                    # 빌드
./gradlew bootRun                  # 로컬 실행 (H2, 기본 프로파일)
./gradlew bootRun --args='--spring.profiles.active=product'  # 운영 프로파일
./gradlew test                     # 전체 테스트
./gradlew test --tests "com.team1.hrbank.SomeTest"  # 단일 테스트
./gradlew clean                    # 빌드 산출물 삭제
```

## 프로파일 구성

| 프로파일 | DB | 설정 파일 | ddl-auto |
|---------|-----|----------|----------|
| `local` (기본값) | H2 in-memory (`jdbc:h2:mem:testdb`) | `application-local.yml` | `create` (재시작 시 DB 초기화) |
| `product` | PostgreSQL | `application-product.yml` | `validate` |

- `product` 프로파일 시 필수 환경변수: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- 로컬 H2 콘솔: `http://localhost:8080/h2-console` (sa / password)
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 아키텍처

### 레이어 흐름

```
Controller → Service (interface) → ServiceImpl → Repository
                                               → FileStorageService (디스크 I/O)
```

### 코딩 컨벤션

**엔티티:**
- `BaseEntity` (id + createdAt) → `BaseUpdatableEntity` (+ updatedAt) 상속 구조
- JPA Auditing으로 `createdAt`, `updatedAt` 자동 관리 (`JpaConfig`에서 `@EnableJpaAuditing`)
- Lombok: `@Getter`, `@Builder`, `@NoArgsConstructor(access = PROTECTED)`, `@AllArgsConstructor`
- Setter 없이 명시적 update 메서드로 필드 변경 (예: `updateFilePath()`)

**서비스:**
- Interface + Impl 분리 패턴
- 클래스 레벨 `@Transactional(readOnly = true)` 기본, 쓰기 메서드에만 `@Transactional` 추가
- 생성자 주입 (`@RequiredArgsConstructor`)

**DTO:**
- Java Record 사용 (`public record SomeDto(...)`)
- 엔티티를 직접 노출하지 않고 서비스/컨트롤러에서 DTO로 변환

**예외 처리:**
- `ResourceNotFoundException` → 404 (리소스 미존재)
- `IllegalArgumentException` → 400 (잘못된 입력)
- `MaxUploadSizeExceededException` → 400 (파일 크기 초과)
- `RuntimeException` → 500 (서버 오류)
- 응답 형식: `{ "message": "..." }` (`GlobalExceptionHandler.ErrorResponse` record)

### 파일 저장 흐름

`BinaryContentService.create()` 3단계:
1. DB에 메타데이터 저장 (filePath 임시 빈 문자열) → ID 확보
2. `FileStorageService.save(id, bytes)` — ID를 파일명으로 디스크에 저장 (`./file-data-map/{id}`)
3. `entity.updateFilePath(savedPath)` — JPA Dirty Checking으로 자동 UPDATE

### DB 스키마 (schema.sql)

현재 구현된 엔티티: `binary_contents`만 JPA Entity 존재.
아직 미구현: `departments`, `employees`, `employee_histories`, `backups` (schema.sql에 DDL만 정의됨).

**테이블 관계:**
- `employees.department_id` → `departments.id` (FK, ON DELETE RESTRICT)
- `employees.profile_image_id` → `binary_contents.id` (FK, ON DELETE SET NULL)
- `backups.file_id` → `binary_contents.id` (FK, ON DELETE SET NULL)
- `employees.status` CHECK: `ACTIVE`, `ON_LEAVE`, `RESIGNED`
- `employee_histories.type` CHECK: `CREATED`, `UPDATED`, `DELETED`

### 파일 업로드 제한

- 단일 파일: 10MB / 요청 전체: 30MB
- 저장 경로: `./file-data-map` (`file.upload-directory` 프로퍼티)
