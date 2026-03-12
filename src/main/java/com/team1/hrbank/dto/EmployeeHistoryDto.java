package com.team1.hrbank.dto;

import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;

public record EmployeeHistoryDto(
    long id,
    //기존 String 에서 HistoryType으로 enum을 받게
    HistoryType type,
    String employeeNumber,
    String memo,
    String ipAddress,
    Instant at
) {

}
