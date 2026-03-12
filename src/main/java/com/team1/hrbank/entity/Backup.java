package com.team1.hrbank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "backup")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Backup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String worker;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BackupStatus status;

  public static Backup startNew(String worker) {
    Backup backup = new Backup();
    backup.worker = worker;
    backup.createdAt = LocalDateTime.now();
    backup.status = BackupStatus.IN_PROGRESS;
    return backup;
  }

  public static Backup skipped(String worker) {
    Backup backup = new Backup();
    backup.worker = worker;
    backup.createdAt = LocalDateTime.now();
    backup.updatedAt = LocalDateTime.now();
    backup.status = BackupStatus.SKIPPED;
    return backup;
  }
}
