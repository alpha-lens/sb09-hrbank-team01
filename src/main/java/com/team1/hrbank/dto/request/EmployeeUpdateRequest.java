package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeUpdateRequest(
    String name,
    String email,
    long departmentId,
    String position,
    LocalDate hireDate,
    EmployeeStatus status,
    String memo
) {

}
