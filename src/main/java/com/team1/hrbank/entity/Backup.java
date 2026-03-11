package com.team1.hrbank.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "backup")
public class Backup {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String worker;

  private LocalDateTime createdAt;

  private LocalDateTime endedAt;

  @Enumerated(EnumType.STRING)
  private BackupStatus status;

  @ManyToOne
  @JoinColumn(name = "file_id")
  private BinaryContent file;
}
