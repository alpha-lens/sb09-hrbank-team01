package com.team1.hrbank.service;

import com.team1.hrbank.entity.Employee;
import java.util.List;

public interface EmployeeService {
  public void createEmployee(EmployeeCreateRequest request);

  public void updateEmployee(EmployeeUpdateRequest request);

  public Employee findEmployee(Long id);

  public List<Employee> findAllEmployees();

  public void deleteEmployee(Long id);

}
