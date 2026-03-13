package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BackupRepository
    extends JpaRepository<Backup, Long>,
    JpaSpecificationExecutor<Backup> {

  Optional<Backup> findTopByStatusOrderByStartedAtDesc(BackupStatus status);

  boolean existsByStatus(BackupStatus status);
}
