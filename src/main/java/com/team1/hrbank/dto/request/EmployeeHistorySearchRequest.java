package com.team1.hrbank.dto.request;

import java.time.Instant;
import lombok.Builder;

@Builder
public record EmployeeHistorySearchRequest(
    String employeeNumber,
    String type,
    String memo,
    String ipAddress,
    Instant atFrom,
    Instant atTo,
    Long idAfter,
    Long cursor,
    int size,
    String sortField,
    String sortDirection
) {
  public EmployeeHistorySearchRequest {
    if (size <= 0)
      size = 10;
    if (sortField == null)
      sortField = "at";
    if (sortDirection == null)
      sortDirection = "desc";
  }
}
