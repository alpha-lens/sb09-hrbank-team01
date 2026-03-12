package com.team1.hrbank.dto.request;

public record DepartmentUpdateRequest(
    String name,
    String description,
    String establishedDate
) {

}
