package com.team1.hrbank.dto;

import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;
import java.util.List;

public record EmployeeHistoryDetailDto(
    long id,
    HistoryType type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at,
    List<DiffDto> diffs
) {

}
