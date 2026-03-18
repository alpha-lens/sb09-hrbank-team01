package com.team1.hrbank.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record EmployeeSearchRequest(
    String nameOrEmail,
    String employeeNumber,
    String departmentName,
    String position,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateFrom,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateTo,
    String status,
    Long idAfter,
    String cursor,
    Integer size,
    String sortField,
    String sortDirection
) {
  
  public EmployeeSearchRequest {
    if (size == null) size = 10;
    if (sortDirection == null) sortDirection = "asc";

    if(idAfter == null && cursor != null && !cursor.isBlank()) {
      try {
        idAfter = Long.parseLong(cursor);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("잘못된 id 값입니다: " + cursor);
      }
    }
  }
}
