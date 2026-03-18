package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCountRequestDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeDistributionRequestDto;
import com.team1.hrbank.dto.request.EmployeeSearchRequest;
import com.team1.hrbank.dto.request.EmployeeTrendRequestDto;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {
  private final EmployeeService employeeService;

  @GetMapping
  public CursorPageResponseEmployeeDto getEmployees(
      @ModelAttribute EmployeeSearchRequest request
  ) {
    return employeeService.findAllEmployees(request);
  }

  @GetMapping("/{id}")
  public EmployeeDto getEmployee(@PathVariable long id) {
    return employeeService.findEmployee(id);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EmployeeDto> createEmployee(
      @RequestPart("employee") EmployeeCreateRequest employeeCreateRequest,
      @RequestPart(required = false) MultipartFile profile
  ) throws IOException {
    return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employeeCreateRequest, profile));
  }

  @PatchMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public EmployeeDto updateEmployee(@PathVariable long id,
      @RequestPart("employee") EmployeeUpdateRequest employeeUpdateRequest,
      @RequestPart(required = false) MultipartFile profile) throws IOException {
    return employeeService.updateEmployee(id, employeeUpdateRequest, profile);
  }

  @DeleteMapping("/{id}")
  public void deleteEmployee(@PathVariable long id, HttpServletRequest httpRequest) {
    String ipAddress = httpRequest.getRemoteAddr();
    employeeService.deleteEmployee(id, ipAddress);
  }

  @GetMapping("/stats/trend")
  public List<EmployeeTrendDto> getEmployeeTrend(@ModelAttribute EmployeeTrendRequestDto request) {
    return employeeService.findEmployeeTrend(request.startDate(), request.endDate(),
        request.unit());
  }

  @GetMapping("/stats/distribution")
  public List<EmployeeDistributionDto> getEmployeeDistribution(
      @ModelAttribute EmployeeDistributionRequestDto request
  ) {
    return employeeService.findEmployeeDistribution(
        request.startDate(), request.endDate(), request.distribution(), request.status()
    );
  }

  @GetMapping("/count")
  public long countEmployees(
      @ModelAttribute EmployeeCountRequestDto request
  ) {
    return employeeService.findEmployeeCount(request.status(), request.fromDate(),
        request.endDate());
  }
}
