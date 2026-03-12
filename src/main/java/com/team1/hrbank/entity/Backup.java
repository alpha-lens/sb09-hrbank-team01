package com.team1.hrbank.entity;

import com.team1.hrbank.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Backup extends BaseEntity {  // ① id, createdAt 상속

  @Column(nullable = false, length = 50)
  private String worker;

  @Column(nullable = false)
  private Instant startedAt;  // ② Instant로 통일 (BaseEntity가 Instant 사용)

  private Instant endedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BackupStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "file_id")
  private BinaryContent backupFile;

  /* ── 팩토리 메서드 ─────────────────────────── */

  public static Backup startNew(String worker) {
    Backup backup   = new Backup();
    backup.worker    = worker;
    backup.startedAt = Instant.now();
    backup.status    = BackupStatus.IN_PROGRESS;
    return backup;
  }

  public static Backup skipped(String worker) {
    Backup backup   = new Backup();
    backup.worker    = worker;
    backup.startedAt = Instant.now();
    backup.endedAt   = Instant.now();
    backup.status    = BackupStatus.SKIPPED;
    return backup;
  }

  /* ── 상태 전이 메서드 ──────────────────────── */

  public void complete(BinaryContent file) {
    this.status     = BackupStatus.COMPLETED;
    this.endedAt    = Instant.now();
    this.backupFile = file;
  }

  public void fail(BinaryContent errorLog) {
    this.status     = BackupStatus.FAILED;
    this.endedAt    = Instant.now();
    this.backupFile = errorLog;
  }
}
