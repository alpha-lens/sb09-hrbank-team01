package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.BackupDownloadDto;
import com.team1.hrbank.dto.BackupDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseBackupDto;
import com.team1.hrbank.dto.request.BackupSearchRequest;
import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.mapper.BackupMapper;
import com.team1.hrbank.repository.BackupRepository;
import com.team1.hrbank.repository.BinaryContentRepository;
import com.team1.hrbank.repository.EmployeeHistoryRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.repository.specification.BackupSpecification;
import com.team1.hrbank.service.BackupService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BackupServiceImpl implements BackupService {

  private final BackupRepository backupRepository;
  private final EmployeeRepository employeeRepository;
  private final BinaryContentRepository binaryContentRepository;
  private final BackupMapper backupMapper;
  private final EmployeeHistoryRepository employeeHistoryRepository;

  @Value("${backup.dir:./backups}")
  private String backupDir;

  @Value("${backup.chunk-size:1000}")
  private int chunkSize;

  @Override
  public BackupDto runBackup(String worker) {

    if (!isBackupNeeded()) {
      log.info("[Backup] 변경사항 없음 → SKIPPED (worker={})", worker);
      Backup backup = Backup.startNew(worker);
      Backup.skipped(backup);
      // 수정 — saved 반환값 사용
      Backup saved = saveBackupInNewTransaction(backup);
      return backupMapper.toDto(saved);
    }

    Backup backup = saveInProgress(worker);
    log.info("[Backup] 시작 id={}, worker={}", backup.getId(), worker);

    Path tempFilePath = null;
    try {
      tempFilePath = performBackup(backup.getId());

      BinaryContent csvFile = saveBinaryContent(tempFilePath, "text/csv");

      Long maxHistoryId = employeeHistoryRepository.findMaxId().orElse(0L);
      backup.complete(csvFile, maxHistoryId);

      Backup saved = saveBackupInNewTransaction(backup);
      log.info("[Backup] 완료 id={}, file={}", saved.getId(), tempFilePath);
      return backupMapper.toDto(saved);

    } catch (Exception e) {
      log.error("[Backup] 실패 id={}", backup.getId(), e);

      deleteFile(tempFilePath);

      BinaryContent errorLog = saveErrorLog(backup.getId(), e);
      backup.fail(errorLog);

      Backup saved = saveBackupInNewTransaction(backup);
      return backupMapper.toDto(saved);
    }
  }

  // 수정 — flush 추가로 즉시 DB 반영 후 saved 반환
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Backup saveBackupInNewTransaction(Backup backup) {
    Backup saved = backupRepository.save(backup);
    backupRepository.flush();
    return saved;
  }

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

    Sort sort = "endedAt".equalsIgnoreCase(sortField)
        ? Sort.by(Sort.Direction.DESC, "endedAt", "id")
        : Sort.by(Sort.Direction.DESC, "startedAt", "id");

    Specification<Backup> spec = BackupSpecification.findByCondition(req);
    Pageable pageable = PageRequest.of(0, size + 1, sort);
    List<Backup> results = backupRepository.findAll(spec, pageable).getContent();

    boolean hasNext = results.size() > size;
    if (hasNext) {
      results = results.subList(0, size);
    }

    BackupSearchRequest countReq = new BackupSearchRequest(
        worker, startedAtFrom, startedAtTo, status, sortField, null, size);
    long totalElements = backupRepository.count(
        BackupSpecification.findByCondition(countReq));

    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !results.isEmpty()) {
      Backup last = results.get(results.size() - 1);
      nextIdAfter = last.getId();
      nextCursor = "endedAt".equalsIgnoreCase(sortField)
          ? (last.getEndedAt() != null ? last.getEndedAt().toString() : null)
          : last.getStartedAt().toString();
    }

    return new CursorPageResponseBackupDto(
        results.stream().map(backupMapper::toDto).toList(),
        nextCursor,
        nextIdAfter,
        size,
        totalElements,
        hasNext
    );
  }

  private boolean isBackupNeeded() {
    Optional<Backup> lastCompleted = backupRepository
        .findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED);

    if (lastCompleted.isEmpty()) return true;

    Long savedHistoryId = lastCompleted.get().getLastHistoryId();
    if (savedHistoryId == null) savedHistoryId = 0L;

    Long currentMaxHistoryId = employeeHistoryRepository.findMaxId().orElse(0L);

    return currentMaxHistoryId > savedHistoryId;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Backup saveInProgress(String worker) {
    if (backupRepository.existsByStatus(BackupStatus.IN_PROGRESS)) {
      throw new IllegalStateException("이미 진행 중인 백업 작업이 있습니다.");
    }
    return backupRepository.save(Backup.startNew(worker));
  }

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

      writer.write("id,employeeNumber,name,email,departmentName,position,hireDate,status");
      writer.newLine();

      int page = 0;
      Page<Employee> chunk;

      do {
        Pageable pageable = PageRequest.of(page++, chunkSize, Sort.by("id"));
        chunk = employeeRepository.findAllWithDepartment(pageable);

        for (Employee emp : chunk.getContent()) {
          writer.write(toCsvRow(emp));
          writer.newLine();
        }

      } while (chunk.hasNext());
    }

    return filePath;
  }

  private BinaryContent saveBinaryContent(Path filePath, String contentType) throws IOException {
    BinaryContent content = BinaryContent.builder()
        .fileName(filePath.getFileName().toString())
        .contentType(contentType)
        .size(Files.size(filePath))
        .filePath(filePath.toString())
        .build();
    return binaryContentRepository.save(content);
  }

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
      return null;
    }
  }

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

  @Override
  @Transactional(readOnly = true)
  public BackupDto getLatest() {
    return backupRepository
        .findTopByStatusOrderByStartedAtDesc(BackupStatus.COMPLETED)
        .map(backupMapper::toDto)
        .orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public BackupDownloadDto getDownloadInfo(Long id) {
    Backup backup = backupRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("백업을 찾을 수 없습니다."));

    if (backup.getBackupFile() == null) {
      return null;
    }

    BinaryContent file = backup.getBackupFile();
    return new BackupDownloadDto(
        file.getFileName(),
        file.getContentType(),
        file.getFilePath()
    );
  }
}
