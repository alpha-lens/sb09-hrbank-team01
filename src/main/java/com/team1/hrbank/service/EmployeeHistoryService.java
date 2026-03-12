package com.team1.hrbank.service;

import com.team1.hrbank.entity.EmployeeHistory;
import java.util.List;

public interface EmployeeHistoryService {
  public void createEmployeeHistory(EmployeeHistoryCreateRequest request);

  public EmployeeHistory findEmployeeHistory(Long id);

  public List<EmployeeHistory> findAllEmployeeHistorys();

  public List<EmployeeHistory> findEmployeeHistoryByRevisionsBetween(Long fromRevision, Long toRevision);

}
