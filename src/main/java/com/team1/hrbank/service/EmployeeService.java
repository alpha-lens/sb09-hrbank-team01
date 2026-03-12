package com.team1.hrbank.service;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import java.util.List;

public interface EmployeeService {

  EmployeeDto createEmployee(EmployeeCreateRequest request);

  EmployeeDto updateEmployee(EmployeeUpdateRequest request);

  EmployeeDto findEmployee(Long id);

  List<EmployeeDto> findAllEmployees();

  void deleteEmployee(Long id);

}
