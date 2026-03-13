package com.team1.hrbank.service;

import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.entity.HistoryType;
import java.time.Instant;
import java.util.List;

public interface EmployeeHistoryService {

  void createEmployeeHistory(EmployeeHistoryCreateRequest request, String ipAddress);

  EmployeeHistoryDetailDto findEmployeeHistory(Long id);

  List<EmployeeHistoryDto> findAllEmployeeHistories();

  List<EmployeeHistoryDto> findEmployeeHistoriesByRevisionsBetween(Long fromRevision,
      Long toRevision);

  CursorPageResponseEmployeeHistoryDto findEmployeeHistories(
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
  );

}
