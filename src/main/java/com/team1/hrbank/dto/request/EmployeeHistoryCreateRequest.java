package com.team1.hrbank.dto.request;

import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.entity.HistoryType;
import java.util.List;

public record EmployeeHistoryCreateRequest(
    HistoryType type,
    String employeeNumber,
    List<DiffDto> diffs,
    String memo
) {}