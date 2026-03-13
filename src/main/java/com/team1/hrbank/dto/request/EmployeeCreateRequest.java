package com.team1.hrbank.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record EmployeeCreateRequest(
    String name,
    String email,
    Long departmentId,
    String position,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate hireDate,
    String jobTitle
) {
}
