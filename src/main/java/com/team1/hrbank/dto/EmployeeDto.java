package com.team1.hrbank.dto;

import com.team1.hrbank.entity.EmployeeStatus;
import java.time.LocalDate;

public record EmployeeDto(
    long id,
    String name,
    String email,
    String employeeNumber,
    long departmentId,
    String departmentName,
    String position,
    LocalDate hireDate,
    EmployeeStatus status,
    long profileImageId
) {

}
