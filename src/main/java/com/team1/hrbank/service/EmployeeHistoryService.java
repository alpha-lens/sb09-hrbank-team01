package com.team1.hrbank.service;

import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.dto.request.EmployeeHistorySearchRequest;
import java.time.Instant;

public interface EmployeeHistoryService {

  void createEmployeeHistory(EmployeeHistoryCreateRequest request, String ipAddress);

  EmployeeHistoryDetailDto findEmployeeHistory(Long id);

  CursorPageResponseEmployeeHistoryDto findEmployeeHistories(EmployeeHistorySearchRequest request);

  long countEmployeeHistories(Instant fromDate, Instant toDate);

}
