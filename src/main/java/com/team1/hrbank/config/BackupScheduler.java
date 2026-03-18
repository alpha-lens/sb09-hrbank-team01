package com.team1.hrbank.config;

import com.team1.hrbank.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduler {

  private final BackupService backupService;

  @Scheduled(fixedRate = 3600000)
  public void autoBackup() {
    log.info("[Backup] 자동 백업 시작");
    try {
      backupService.runBackup("system");
    } catch (Exception e) {
      log.error("[Backup] 자동 백업 실패", e);
    }
  }
}
