package com.team1.hrbank.dto;

public record EmployeeDto(
    long id,
    String name,
    String email,
    String employeeNumber,
    long departmentId,
    String departmentName,
    String position,
    String hireDate,
    String status,
    long profileImageId
) {

}
