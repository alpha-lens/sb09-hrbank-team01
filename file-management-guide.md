# 파일 관리 API 가이드

## 개요

파일 업로드/다운로드/삭제를 담당하는 `BinaryContent` 모듈 사용법입니다.
다른 도메인(Employee 프로필 이미지, Backup 파일 등)에서 파일이 필요할 때 이 모듈을 사용하세요.

---

## 1. API 엔드포인트

| 메서드 | 경로 | 설명 | 응답 코드 |
|--------|------|------|----------|
| `POST` | `/api/files` | 파일 업로드 | 201 Created |
| `GET` | `/api/files/{id}` | 메타데이터 조회 | 200 OK |
| `GET` | `/api/files/{id}/download` | 파일 다운로드 | 200 OK |
| `DELETE` | `/api/files/{id}` | 파일 삭제 | 204 No Content |

### 1-1. 파일 업로드

```bash
curl -X POST http://localhost:8080/api/files \
  -F "file=@./내파일.png"
```

**응답 (201):**
```json
{
  "id": 1,
  "fileName": "내파일.png",
  "size": 102400,
  "contentType": "image/png"
}
```

### 1-2. 메타데이터 조회

```bash
curl http://localhost:8080/api/files/1
```

**응답 (200):**
```json
{
  "id": 1,
  "fileName": "내파일.png",
  "size": 102400,
  "contentType": "image/png"
}
```

### 1-3. 파일 다운로드

```bash
curl -OJ http://localhost:8080/api/files/1/download
```

- `Content-Type`: 파일의 원본 MIME 타입
- `Content-Disposition`: `attachment; filename*=UTF-8''인코딩된파일명`

### 1-4. 파일 삭제

```bash
curl -X DELETE http://localhost:8080/api/files/1
```

- DB 레코드 + 디스크 파일 모두 삭제됨
- 존재하지 않는 ID로 삭제 요청하면 아무 일도 일어나지 않음 (204 반환)

---

## 2. 에러 응답

모든 에러는 동일한 형식으로 반환됩니다.

```json
{
  "message": "에러 메시지"
}
```

| 상황 | 상태 코드 | 메시지 |
|------|----------|--------|
| 존재하지 않는 파일 조회/다운로드 | 404 | `파일을 찾을 수 없습니다. id=123` |
| 빈 파일 업로드 | 400 | `업로드된 파일이 없습니다.` |
| 파일 크기 초과 (10MB) | 400 | `파일 크기가 허용 한도를 초과했습니다.` |
| 서버 내부 오류 | 500 | 상세 에러 메시지 |

---

## 3. 파일 제한사항

- 단일 파일 최대: **10MB**
- 요청 전체 최대: **30MB**
- 디스크 저장 경로: `./file-data-map/` (프로젝트 루트 기준)
- 파일명 형식: 파일 ID를 그대로 사용 (예: `./file-data-map/1`, `./file-data-map/2`)

---

## 4. 다른 도메인에서 파일 관리 연동하기

### 4-1. 서비스 계층에서 직접 호출

다른 서비스에서 `BinaryContentService`를 주입받아 사용하세요.

```java
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final BinaryContentService binaryContentService;

    @Transactional
    public Employee createEmployee(EmployeeCreateRequest request, MultipartFile profileImage) {
        // 프로필 이미지 저장
        BinaryContent image = binaryContentService.create(profileImage);

        // Employee 엔티티에 파일 ID 연결
        Employee employee = Employee.builder()
                .name(request.name())
                .profileImageId(image.getId())
                .build();

        return employeeRepository.save(employee);
    }
}
```

### 4-2. BinaryContentService 인터페이스

```java
public interface BinaryContentService {

    // 파일 저장 (DB 메타데이터 + 디스크 파일)
    BinaryContent create(MultipartFile file);

    // 파일 바이트 반환 (다운로드용)
    byte[] getBytes(Long id);

    // 메타데이터 DTO 반환
    BinaryContentDto getMetadata(Long id);

    // 파일 삭제 (DB + 디스크)
    void delete(Long id);
}
```

### 4-3. Entity에서 FK 연결 예시

```java
@Entity
public class Employee extends BaseUpdatableEntity {

    // BinaryContent와 FK 관계 (schema.sql 참고)
    @Column(name = "profile_image_id")
    private Long profileImageId;

    // 또는 JPA 관계 매핑
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_image_id")
    private BinaryContent profileImage;
}
```

**schema.sql 참고 — employees 테이블:**
```sql
profile_image_id  BIGINT,  -- NULL 허용 (선택적)
CONSTRAINT fk_employee_profile_image
    FOREIGN KEY (profile_image_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
```

**schema.sql 참고 — backups 테이블:**
```sql
file_id  BIGINT,  -- NULL 허용
CONSTRAINT fk_backup_file
    FOREIGN KEY (file_id)
    REFERENCES binary_contents (id)
    ON DELETE SET NULL
```

---

## 5. 파일 구조

```
com.team1.hrbank
├── controller/
│   └── BinaryContentController.java   ← REST 엔드포인트 4개
├── dto/
│   └── BinaryContentDto.java          ← 응답 DTO (id, fileName, size, contentType)
├── entity/
│   └── BinaryContent.java             ← JPA 엔티티 (binary_contents 테이블)
├── repository/
│   └── BinaryContentRepository.java   ← JpaRepository<BinaryContent, Long>
├── service/
│   ├── BinaryContentService.java      ← 인터페이스 (다른 서비스에서 이것을 주입)
│   └── BinaryContentServiceImpl.java  ← 구현체
├── storage/
│   └── FileStorageService.java        ← 디스크 파일 읽기/쓰기/삭제
└── global/
    ├── GlobalExceptionHandler.java    ← 전역 예외 처리
    └── ResourceNotFoundException.java ← 404 커스텀 예외
```

---

## 6. DB 스키마

```sql
CREATE TABLE binary_contents (
    id            BIGSERIAL    PRIMARY KEY,
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size          BIGINT       NOT NULL,
    file_path     VARCHAR(500) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);
```

| 컬럼 | 설명 |
|------|------|
| `id` | PK, 자동 증가 |
| `file_name` | 원본 파일명 (예: `profile.png`) |
| `content_type` | MIME 타입 (예: `image/png`) |
| `size` | 파일 크기 (byte) |
| `file_path` | 디스크 절대 경로 |
| `created_at` | 생성 시각 (JPA Auditing) |

---

## 7. 테스트 시 참고사항

### Swagger UI로 테스트
브라우저에서 `http://localhost:8080/swagger-ui.html` 접속 → `binary-content-controller` 섹션에서 직접 테스트 가능

### 로컬 DB 확인
`http://localhost:8080/h2-console` 접속
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

```sql
SELECT * FROM binary_contents;
```

### 주의사항
- 로컬(local) 프로파일은 `ddl-auto: create`이므로 **서버 재시작 시 DB가 초기화**됩니다
- DB 초기화 시 `./file-data-map/` 디렉토리의 파일은 남아있으므로, 필요하면 수동 삭제하세요
- 테스트 시 파일 업로드 후 반환된 `id` 값을 사용하여 조회/다운로드/삭제하세요
