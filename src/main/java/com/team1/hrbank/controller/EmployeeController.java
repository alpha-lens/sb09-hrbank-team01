package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeTrendRequestDto;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.service.EmployeeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeService employeeService;

  @GetMapping
  public List<EmployeeDto> getEmployees() {
    return employeeService.findAllEmployees();
  }

  @GetMapping("/{id}")
  public EmployeeDto getEmployee(@PathVariable long id) {
    return employeeService.findEmployee(id);
  }

  @PostMapping
  public EmployeeDto createEmployee(@RequestBody EmployeeCreateRequest employeeCreateRequest) {
    return employeeService.createEmployee(employeeCreateRequest);
  }

  @PatchMapping("/{id}")
  public EmployeeDto updateEmployee(@PathVariable long id,
      @RequestBody EmployeeUpdateRequest employeeUpdateRequest) {
    return employeeService.updateEmployee(id, employeeUpdateRequest);
  }

  @DeleteMapping("/{id}")
  public void deleteEmployee(@PathVariable long id) {
    employeeService.deleteEmployee(id);
  }

  @GetMapping("/stats/trend")
  public List<EmployeeTrendDto> getEmployeeTrend(@RequestBody EmployeeTrendRequestDto request) {
    return employeeService.findEmployeeTrend(request.startDate(), request.endDate(),
        request.unit());
  }

  @GetMapping("/stats/disctribution")
  public EmployeeDistributionDto getEmployeeDistribution() {
    return null;
  }

  @GetMapping("/count")
  public Long countEmployees() {
    return null;
  }
}
