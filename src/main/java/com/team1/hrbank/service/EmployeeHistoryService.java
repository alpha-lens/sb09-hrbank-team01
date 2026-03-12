package com.team1.hrbank.service;

import java.util.List;

public interface EmployeeHistoryService {

  EmployeeHistoryDto createEmployeeHistory(EmployeeHistoryCreateRequest request);

  EmployeeHistoryDto findEmployeeHistory(Long id);

  List<EmployeeHistoryDto> findAllEmployeeHistories();

  List<EmployeeHistoryDto> findEmployeeHistoriesByRevisionsBetween(Long fromRevision,
      Long toRevision);

}
