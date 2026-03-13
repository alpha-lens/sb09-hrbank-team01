package com.team1.hrbank.dto.request;

import java.time.LocalDate;

public record DepartmentCreateRequest(
    String name,
    String description,
    LocalDate establishedDate
) {

}
