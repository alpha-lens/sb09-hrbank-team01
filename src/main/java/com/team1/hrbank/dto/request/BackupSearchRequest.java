package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.BackupStatus;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackupSearchRequest {

  private String worker;
  private Instant startedAtFrom;
  private Instant startedAtTo;
  private BackupStatus status;

  private String sortField = "startedAt";
  private Long   lastId;
  private int size = 10;
}