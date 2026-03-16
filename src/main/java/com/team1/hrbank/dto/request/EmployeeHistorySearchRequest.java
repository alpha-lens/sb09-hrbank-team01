package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;
import lombok.Builder;

@Builder
public record EmployeeHistorySearchRequest(
    String employeeNumber,
    HistoryType type,
    String memo,
    String ipAddress,
    Instant atFrom,
    Instant atTo,
    Long idAfter,
    String cursor,
    int size,
    String sortField,
    String sortDirection
) {
  public EmployeeHistorySearchRequest {
    if (size == 0)
      size = 10;
    if (sortField == null)
      sortField = "at";
    if (sortDirection == null)
      sortDirection = "desc";
  }
}
