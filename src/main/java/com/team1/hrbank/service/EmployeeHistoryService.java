package com.team1.hrbank.service;

import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import java.util.List;

public interface EmployeeHistoryService {

  EmployeeHistoryDto createEmployeeHistory(EmployeeHistoryCreateRequest request);

  EmployeeHistoryDto findEmployeeHistory(Long id);

  List<EmployeeHistoryDto> findAllEmployeeHistories();

  List<EmployeeHistoryDto> findEmployeeHistoriesByRevisionsBetween(Long fromRevision,
      Long toRevision);

}
