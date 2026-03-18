package com.team1.hrbank.repository;

import com.team1.hrbank.entity.Backup;
import com.team1.hrbank.entity.BackupStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface BackupRepository
    extends JpaRepository<Backup, Long>,
    JpaSpecificationExecutor<Backup> {

  Optional<Backup> findTopByStatusOrderByStartedAtDesc(BackupStatus status);

  List<Backup> findAllByStatus(BackupStatus status);

  boolean existsByStatus(BackupStatus status);

}

