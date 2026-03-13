package com.team1.hrbank.service;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.entity.EmployeeDistribution;
import com.team1.hrbank.entity.EmployeeStatus;
import com.team1.hrbank.entity.EmployeeTrendTimeUnit;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

  EmployeeDto createEmployee(EmployeeCreateRequest request, MultipartFile profileImage);

  EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest request);

  EmployeeDto findEmployee(Long id);

  List<EmployeeDto> findAllEmployees();

  void deleteEmployee(Long id);

  List<EmployeeTrendDto> findEmployeeTrend(LocalDate startDate, LocalDate endDate, EmployeeTrendTimeUnit unit);

  List<EmployeeDistributionDto> findEmployeeDistribution(LocalDate startDate, LocalDate endDate, EmployeeDistribution distribution, EmployeeStatus status);

  long findEmployeeCount(EmployeeStatus status, LocalDate startDate, LocalDate endDate);
}
