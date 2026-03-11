# 파일 관리(File Management) 구현 정리

## 구현 파일 목록

| 파일 | 역할 |
|------|------|
| `entity/BinaryContent.java` | 파일 메타데이터 JPA 엔티티 |
| `repository/BinaryContentRepository.java` | JPA Repository |
| `dto/BinaryContentDto.java` | 파일 메타데이터 응답 DTO |
| `storage/FileStorageService.java` | 디스크 파일 I/O 처리 |
| `service/BinaryContentService.java` | 서비스 인터페이스 |
| `service/BinaryContentServiceImpl.java` | 서비스 구현체 |
| `controller/BinaryContentController.java` | REST Controller |
| `config/WebConfig.java` | CORS 설정 |
| `config/JpaConfig.java` | JPA Auditing 활성화 |
| `global/GlobalExceptionHandler.java` | 전역 예외 처

## 1. BinaryContent 엔티티

**파일:** `entity/BinaryContent.java`

```
binary_contents 테이블 매핑
- BaseEntity 상속 → id(PK), createdAt(자동 기록) 제공
- fileName  : 원본 파일명 (예: "profile.png")
- contentType : MIME 타입 (예: "image/jpeg")
- size      : 바이트 단위 파일 크기
- filePath  : 디스크 저장 경로 (예: "/abs/path/file-data-map/1")
```

**설계 결정:**
- `BaseEntity`를 상속해 id, createdAt을 공통 관리한다.
- `@Builder` + `@NoArgsConstructor(PROTECTED)` + `@AllArgsConstructor` 조합으로 외부에서 직접 생성자 호출을 막고 빌더만 사용한다.
- `filePath`는 DB 저장 후 id가 확정되어야 결정되므로, `updateFilePath()` 메서드를 별도로 제공한다.

---

## 2. FileStorageService

**파일:** `storage/FileStorageService.java`

디스크 I/O 전담 클래스. 비즈니스 로직과 분리한다.

```
저장 경로 설정: application-local.yml → file.upload-directory: ./file-data-map
경로는 서버 기동 시 @Value로 주입받아 절대경로로 정규화한다.
디렉토리가 없으면 자동 생성한다.
```

| 메서드 | 설명 |
|--------|------|
| `save(fileId, bytes)` | `{storageLocation}/{fileId}` 경로에 파일 저장, 저장 경로 반환 |
| `load(fileId)` | 파일 읽어 byte[] 반환 |
| `delete(fileId)` | 파일 삭제 (없으면 무시) |
| `exists(fileId)` | 파일 존재 여부 확인 |

**설계 결정:**
- 파일명을 `fileId`로 고정해 DB 메타데이터와 디스크 파일 간 1:1 매핑을 단순하게 유지한다.
- 별도 UUID/난수 파일명을 쓰지 않아 경로 추적이 쉽다.

---

## 3. BinaryContentServiceImpl - 파일 저장 흐름

**파일:** `service/BinaryContentServiceImpl.java`

### create() 3단계 흐름

```
1단계 - DB에 메타데이터 저장 (filePath = "" 임시값)
        → 이 시점에 id 확보됨

2단계 - fileStorageService.save(id, bytes) 호출
        → {storageLocation}/{id} 경로에 파일 저장
        → 저장된 절대 경로 문자열 반환

3단계 - entity.updateFilePath(savedPath) 호출
        → JPA 더티 체킹으로 트랜잭션 종료 시 UPDATE 쿼리 자동 실행
```

**왜 두 번에 나눠 저장하는가?**

`BIGSERIAL`로 자동 생성되는 `id`를 파일명으로 사용하려면 DB INSERT 이후에야 id를 알 수 있다.
따라서 ① 먼저 DB에 저장해 id를 받고 → ② 그 id로 디스크에 저장하는 순서가 불가피하다.

**트랜잭션 롤백 주의:**
DB 롤백은 JPA가 처리하지만, 디스크에 이미 저장된 파일은 자동으로 삭제되지 않는다.
예외 발생 시 `fileStorageService.delete(id)` 를 호출하는 보상 로직 추가를 권장한다.

---

## 4. BinaryContentController - 다운로드 API

**파일:** `controller/BinaryContentController.java`

```
GET /api/files/{id}/download
```

**응답 헤더 구성:**

```
Content-Type       : 파일의 MIME 타입 (DB에 저장된 contentType)
Content-Length     : 파일 바이트 크기
Content-Disposition: attachment; filename*=UTF-8''인코딩된파일명
```

**Content-Disposition 형식 선택 이유:**
프론트엔드(`fileApi.ts`)가 다음 정규식으로 파일명을 파싱하기 때문에 RFC 5987 형식이 필수다.
```
/filename\*?=(?:UTF-8''|")?([^";]+)/
```
Spring의 `ContentDisposition.builder("attachment").filename(name, UTF_8).build()`를 사용하면
자동으로 `filename*=UTF-8''인코딩된파일명` 형식으로 생성된다.

---

## 5. WebConfig - CORS 설정

**파일:** `config/WebConfig.java`

```
허용 Origin : http://localhost:5173 (Vite), http://localhost:3000
허용 Method : GET, POST, PUT, PATCH, DELETE, OPTIONS
허용 Headers: *
Credentials : true
```

---

## 6. JpaConfig - JPA Auditing 활성화

**파일:** `config/JpaConfig.java`

`BaseEntity`의 `@CreatedDate`가 동작하려면 `@EnableJpaAuditing`이 반드시 필요하다.
`HrbankApplication`에 직접 붙이지 않고 별도 Config 클래스로 분리했다.
(테스트 시 애플리케이션 컨텍스트 로딩 문제를 방지하기 위함)

---

## 7. GlobalExceptionHandler

**파일:** `global/GlobalExceptionHandler.java`

| 예외 | HTTP 상태 |
|------|-----------|
| `IllegalArgumentException` | 400 Bad Request |
| `RuntimeException` | 500 Internal Server Error |

응답 바디: `{ "message": "에러 메시지" }`

---

## 8. 타 팀 연동 방법

파일 팀이 제공하는 `BinaryContentService.create(MultipartFile)`를 호출하면 된다.

```java
// 직원 팀 - 프로필 이미지 저장 예시
BinaryContent savedFile = binaryContentService.create(profileFile);
Long profileImageId = savedFile.getId();  // employees.profile_image_id에 저장

// 백업 팀 - CSV 파일 저장 예시
MockMultipartFile mockFile = new MockMultipartFile(
    "backup",
    "backup_2026-03-11.csv",
    "application/octet-stream",
    csvBytes
);
BinaryContent savedFile = binaryContentService.create(mockFile);
Long fileId = savedFile.getId();  // backups.file_id에 저장
```

---

## 9. 로컬 테스트 방법

```bash
# 서버 실행 (H2 인메모리 DB 사용)
./gradlew bootRun

# 파일 다운로드 테스트 (id=1 파일이 존재할 때)
curl -X GET http://localhost:8080/api/files/1/download --output downloaded.jpg

# H2 콘솔에서 binary_contents 테이블 확인
# http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:testdb / SA / password
```
