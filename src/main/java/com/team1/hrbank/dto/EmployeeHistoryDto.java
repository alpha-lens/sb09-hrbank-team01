package com.team1.hrbank.dto;

import java.time.Instant;

public record EmployeeHistoryDto(
    long id,
    String type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at
) {

}
