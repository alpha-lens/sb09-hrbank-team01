package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.HistoryType;

public record EmployeeHistoryCreateRequest(
    HistoryType type,
    String employeeNumber,
    String diffJson,
    String memo,
    String ipAddress
) {

}
