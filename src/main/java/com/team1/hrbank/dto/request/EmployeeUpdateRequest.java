package com.team1.hrbank.dto.request;

public record EmployeeUpdateRequest(
    String name,
    String email,
    Integer departmentId,
    String position,
    String hireDate,
    String status,
    String memo
) {

}
