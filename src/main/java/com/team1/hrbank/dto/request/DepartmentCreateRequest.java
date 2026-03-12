package com.team1.hrbank.dto.request;

public record DepartmentCreateRequest(
    String name,
    String description,
    String establishedDate
) {

}
