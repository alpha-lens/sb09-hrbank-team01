package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.BackupDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseBackupDto;
import com.team1.hrbank.dto.request.BackupSearchRequest;
import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.mapper.BackupMapper;
import com.team1.hrbank.repository.BackupRepository;
import com.team1.hrbank.repository.Specification.BackupSpecification;
import com.team1.hrbank.repository.BinaryContentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.service.BackupService;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

  private final BackupRepository backupRepository;
  private final EmployeeRepository employeeRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BackupMapper backupMapper;

  @Value("${backup.dir:./backups}")
  private String backupDir;

  @Value("${backup.chunk-size:1000}")
  private int chunkSize;

  /* ── 백업 실행 ───────────────────────────────────────── */
  @Override
  public BackupDto runBackup(String worker) {

    // STEP.1 백업 필요 여부 판단
    if (!isBackupNeeded()) {
      log.info("[Backup] 변경사항 없음 → SKIPPED (worker={})", worker);
      Backup backup = Backup.startNew(worker);
      Backup.skipped(backup);
      return toDto(backupRepository.save(backup));
    }

    // STEP.2 IN_PROGRESS 저장 (즉시 커밋 → API 바로 조회 가능)
    Backup backup = saveInProgress(worker);
    log.info("[Backup] 시작 id={}, worker={}", backup.getId(), worker);

    Path tempFilePath = null;
    try {
      // STEP.3 CSV 파일 생성
      tempFilePath = performBackup(backup.getId());

      // BinaryContent 저장
      BinaryContent csvFile = saveBinaryContent(tempFilePath, "text/csv");

      // STEP.4-1 성공 처리
      backup.complete(csvFile);
      log.info("[Backup] 완료 id={}, file={}", backup.getId(), tempFilePath);
      return toDto(backupRepository.save(backup));

    } catch (Exception e) {
      log.error("[Backup] 실패 id={}", backup.getId(), e);

      // 생성 중이던 CSV 삭제
      deleteFile(tempFilePath);

      // 에러 로그 저장
      BinaryContent errorLog = saveErrorLog(backup.getId(), e);

      // STEP.4-2 실패 처리
      backup.fail(errorLog);
      return toDto(backupRepository.save(backup));
    }
  }

  /* ── 백업 이력 목록 조회 ─────────────────────────────── */
  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseBackupDto getList(
      String worker,
      Instant startedAtFrom,
      Instant startedAtTo,
      BackupStatus status,
      String sortField,
      Long lastId,
      int size) {

    BackupSearchRequest req = new BackupSearchRequest(
        worker, startedAtFrom, startedAtTo, status, sortField, lastId, size);

    // 정렬 조건 결정
    Sort sort = "endedAt".equalsIgnoreCase(sortField)
        ? Sort.by(Sort.Direction.DESC, "endedAt", "id")
        : Sort.by(Sort.Direction.DESC, "startedAt", "id");

    // 동적 조건 + 정렬 적용
    Specification<Backup> spec = BackupSpecification.findByCondition(req);
    Pageable pageable = PageRequest.of(0, size + 1, sort);
    List<Backup> results = backupRepository.findAll(spec, pageable).getContent();

    // size + 1 조회로 hasNext 판별
    boolean hasNext = results.size() > size;
    if (hasNext) {
      results = results.subList(0, size);
    }

    // totalElements (커서 조건 제외)
    BackupSearchRequest countReq = new BackupSearchRequest(
        worker, startedAtFrom, startedAtTo, status, sortField, null, size);
    long totalElements = backupRepository.count(
        BackupSpecification.findByCondition(countReq));

    // 다음 커서 계산
    String nextCursor = null;
    long nextIdAfter = 0L;

    if (hasNext && !results.isEmpty()) {
      Backup last = results.get(results.size() - 1);
      nextIdAfter = last.getId();
      nextCursor = "endedAt".equalsIgnoreCase(sortField)
          ? (last.getEndedAt() != null ? last.getEndedAt().toString() : null)
          : last.getStartedAt().toString();
    }

    return new CursorPageResponseBackupDto(
        results.stream().map(this::toDto).toList(),
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  /* ── STEP.1 백업 필요 여부 판단 ─────────────────────── */
  private boolean isBackupNeeded() {
    Optional<Backup> lastCompleted = backupRepository
        .findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED);

    // 완료된 백업이 한 번도 없으면 → 필요
    if (lastCompleted.isEmpty()) return true;

    Instant lastBackupTime = lastCompleted.get().getStartedAt();

    // 마지막 완료 백업 이후 직원 데이터 변경 여부
    return employeeRepository.existsByUpdatedAtAfter(lastBackupTime);
  }

  /* ── STEP.2 IN_PROGRESS 저장 ────────────────────────── */
  private Backup saveInProgress(String worker) {
    if (backupRepository.existsByStatus(BackupStatus.IN_PROGRESS)) {
      throw new IllegalStateException("이미 진행 중인 백업 작업이 있습니다.");
    }
    return backupRepository.save(Backup.startNew(worker));
  }

  /* ── STEP.3 CSV 파일 생성 (청크 단위로 OOM 방지) ────── */
  private Path performBackup(Long backupId) throws IOException {
    Path dir = Paths.get(backupDir);
    Files.createDirectories(dir);

    String fileName = "backup_%d_%s.csv".formatted(
        backupId,
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now()));

    Path filePath = dir.resolve(fileName);

    try (BufferedWriter writer = Files.newBufferedWriter(filePath,
        StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

      // 헤더 작성
      writer.write("id,employeeNumber,name,email,departmentName,position,hireDate,status");
      writer.newLine();

      // 청크 단위로 읽어서 작성
      int page = 0;
      Page<Employee> chunk;

      do {
        Pageable pageable = PageRequest.of(page++, chunkSize, Sort.by("id"));
        chunk = employeeRepository.findAll(pageable);

        for (Employee emp : chunk.getContent()) {
          writer.write(toCsvRow(emp));
          writer.newLine();
        }

      } while (chunk.hasNext());
    }

    return filePath;
  }

  /* ── BinaryContent 저장 ──────────────────────────────── */
  private BinaryContent saveBinaryContent(Path filePath, String contentType) throws IOException {
    BinaryContent content = BinaryContent.builder()
        .fileName(filePath.getFileName().toString())
        .contentType(contentType)
        .size(Files.size(filePath))
        .filePath(filePath.toString())
        .build();
    return binaryContentRepository.save(content);
  }

  /* ── 에러 로그 저장 ──────────────────────────────────── */
  private BinaryContent saveErrorLog(Long backupId, Exception e) {
    try {
      Path dir = Paths.get(backupDir);
      Files.createDirectories(dir);

      String fileName = "backup_error_%d_%s.log".formatted(
          backupId,
          DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
              .withZone(ZoneId.systemDefault())
              .format(Instant.now()));

      Path logPath = dir.resolve(fileName);

      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      Files.writeString(logPath, sw.toString());

      return saveBinaryContent(logPath, "text/plain");

    } catch (IOException ioEx) {
      log.error("[Backup] 에러 로그 저장 실패 backupId={}", backupId, ioEx);
      return null; // null이 backup.fail()로 전달됨
    }
  }

  /* ── CSV row 변환 ────────────────────────────────────── */
  private String toCsvRow(Employee emp) {
    return String.join(",",
        String.valueOf(emp.getId()),
        escapeCsv(emp.getEmployeeNumber()),
        escapeCsv(emp.getName()),
        escapeCsv(emp.getEmail()),
        escapeCsv(emp.getDepartment().getName()),
        escapeCsv(emp.getPosition()),
        emp.getHireDate().toString(),
        emp.getStatus().name()
    );
  }

  private String escapeCsv(String value) {
    if (value == null) return "";
    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  /* ── 파일 삭제 ───────────────────────────────────────── */
  private void deleteFile(Path filePath) {
    if (filePath != null) {
      try {
        Files.deleteIfExists(filePath);
        log.info("[Backup] 임시 파일 삭제 완료: {}", filePath);
      } catch (IOException e) {
        log.warn("[Backup] 임시 파일 삭제 실패: {}", filePath, e);
      }
    }
  }

  /* ── Backup → BackupDto 변환 ─────────────────────────── */
  private BackupDto toDto(Backup backup) {
    return backupMapper.toDto(backup);
  }
}
