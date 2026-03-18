package com.team1.hrbank.config;

import com.team1.hrbank.entity.BackupStatus;
import com.team1.hrbank.repository.BackupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupInitializer implements ApplicationRunner {

  private final BackupRepository backupRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    backupRepository.findAllByStatus(BackupStatus.IN_PROGRESS)
        .forEach(backup -> {
              backup.fail(null);
              backupRepository.save(backup);
              log.info("[Backup] 미완료 백업 FAILED 처리 id={}", backup.getId());
            }
        );
  }
}
