package com.team1.hrbank.dto;

import com.team1.hrbank.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeDto(
    Long id,
    String name,
    String email,
    String employeeNumber,
    Long departmentId,
    String departmentName,
    String position,
    LocalDate hireDate,
    EmployeeStatus status,
    Long profileImageId
) {

}
