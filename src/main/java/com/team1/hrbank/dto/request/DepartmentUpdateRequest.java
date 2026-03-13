package com.team1.hrbank.dto.request;

import java.time.LocalDate;

public record DepartmentUpdateRequest(
    String newName,
    String newDescription,
    LocalDate newEstablishedDate
) {

}
