package com.team1.hrbank.service;

import com.team1.hrbank.dto.BackupDownloadDto;
import com.team1.hrbank.dto.BackupDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseBackupDto;
import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.time.Instant;
import org.springframework.http.ResponseEntity;

public interface BackupService {

  // 수동/자동 백업 실행
  BackupDto runBackup(String worker);

  BackupDto getLatest();

  BackupDownloadDto getDownloadInfo(Long id);

  // 백업 이력 목록 조회
  CursorPageResponseBackupDto getList(
      String worker,
      Instant startedAtFrom,
      Instant startedAtTo,
      BackupStatus status,
      String sortField,
      Long lastId,
      int size
  );
}
