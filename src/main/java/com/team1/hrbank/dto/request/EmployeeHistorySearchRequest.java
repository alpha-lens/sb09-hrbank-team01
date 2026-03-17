package com.team1.hrbank.dto.request;

import com.team1.hrbank.entity.HistoryType;
import lombok.Builder;

@Builder
public record EmployeeHistorySearchRequest(
    String employeeNumber,
    String type,
    String memo,
    String ipAddress,
    String atFrom,
    String atTo,
    Long idAfter,
    Long cursor,
    Integer size,
    String sortField,
    String sortDirection
) {
  public EmployeeHistorySearchRequest {
    if (size == null || size <= 0)
      size = 10;
    if (sortField == null)
      sortField = "at";
    if (sortDirection == null)
      sortDirection = "desc";
  }

  public HistoryType historyType() {
    if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
      return null;
    }
    return HistoryType.valueOf(type.toUpperCase());
  }
}
