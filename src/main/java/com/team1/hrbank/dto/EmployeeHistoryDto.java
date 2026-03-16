package com.team1.hrbank.dto;

import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;

public record EmployeeHistoryDto(
    long id,
    HistoryType type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at
) {

}
