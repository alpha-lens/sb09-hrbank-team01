package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.BackupStatus;
import java.time.Instant;


public record BackupSearchRequest(
    String worker,
    Instant startedAtFrom,
    Instant startedAtTo,
    BackupStatus status,
    String sortField,
    Long lastId,
    int size
) {

}