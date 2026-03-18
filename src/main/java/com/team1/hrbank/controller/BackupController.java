package com.team1.hrbank.controller;

import com.team1.hrbank.dto.BackupDownloadDto;
import com.team1.hrbank.dto.BackupDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseBackupDto;
import com.team1.hrbank.entity.BackupStatus;
import com.team1.hrbank.service.BackupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/api/backups")
@RequiredArgsConstructor
@Tag(name = "Backup", description = "데이터 백업 API")
public class BackupController {

  @Value("${hrbank.backup.dir}")
  private String backupDir;

  private final BackupService backupService;

  /* ── 수동 백업 실행 ───────────────────────────────────── */
  @PostMapping
  @Operation(summary = "수동 백업 실행", description = "요청자 IP를 worker로 백업을 즉시 실행합니다.")
  public ResponseEntity<BackupDto> runBackup(HttpServletRequest request) {
    String clientIp = resolveClientIp(request);
    log.info("[Backup] 수동 백업 요청 ip={}", clientIp);
    return ResponseEntity.ok(backupService.runBackup(clientIp));
  }

  /* ── 백업 이력 목록 조회 ──────────────────────────────── */
  @GetMapping
  @Operation(summary = "백업 이력 목록 조회", description = "조건별 필터링 및 커서 페이지네이션으로 조회합니다.")
  public ResponseEntity<CursorPageResponseBackupDto> getList(
      @RequestParam(required = false) String worker,
      @RequestParam(required = false) Instant startedAtFrom,
      @RequestParam(required = false) Instant startedAtTo,
      @RequestParam(required = false) BackupStatus status,
      @RequestParam(defaultValue = "startedAt") String sortField,
      @RequestParam(required = false) Long lastId,
      @RequestParam(defaultValue = "10") int size) {

    return ResponseEntity.ok(backupService.getList(
        worker, startedAtFrom, startedAtTo, status, sortField, lastId, size));
  }

  /* ── 요청자 IP 추출 ───────────────────────────────────── */
  private String resolveClientIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(ip)) {
      return ip.split(",")[0].trim();
    }
    String remoteAddr = request.getRemoteAddr();
    if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
      return "127.0.0.1";
    }
    return remoteAddr;
  }

  @GetMapping("/{id}/download")
  @Operation(summary = "백업 파일 다운로드")
  public ResponseEntity<Resource> download(@PathVariable Long id) throws IOException {

    BackupDownloadDto downloadInfo = backupService.getDownloadInfo(id);

    if (downloadInfo == null) {
      return ResponseEntity.notFound().build();
    }

    // Path Traversal 방어
    Path baseDir = Paths.get(backupDir).toAbsolutePath().normalize();
    Path filePath = Paths.get(downloadInfo.filePath())
        .toAbsolutePath().normalize();

    if (!filePath.startsWith(baseDir)) {
      log.warn("[Backup] Path traversal 시도 감지: {}", filePath);
      return ResponseEntity.badRequest().build();
    }

    Resource resource = new FileSystemResource(filePath);
    if (!resource.exists()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(downloadInfo.fileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
        .contentType(MediaType.parseMediaType(downloadInfo.contentType()))
        .body(resource);
  }

  @GetMapping("/latest")
  @Operation(summary = "최신 백업 조회")
  public ResponseEntity<BackupDto> getLatest() {
    BackupDto latest = backupService.getLatest();

    // null이면 204 No Content 반환
    if (latest == null) {
      return ResponseEntity.noContent().build();
    }

    return ResponseEntity.ok(latest);
  }
}
