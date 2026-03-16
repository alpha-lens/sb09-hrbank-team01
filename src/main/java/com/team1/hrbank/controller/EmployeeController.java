package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCountRequestDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeDistributionRequestDto;
import com.team1.hrbank.dto.request.EmployeeTrendRequestDto;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.service.EmployeeService;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

  /* TEST 필요한 부분
   * 1. ModelAttribute를 통해 DTO로 잘 들어오는가?
   * 2. 각 컨트롤러는 잘 작동하는가?
   * 3. 대시보드는 잘 되는가?
   * */

  private final EmployeeService employeeService;

  @GetMapping
  public List<EmployeeDto> getEmployees() {
    return employeeService.findAllEmployees();
  }

  @GetMapping("/{id}")
  public EmployeeDto getEmployee(@PathVariable long id) {
    return employeeService.findEmployee(id);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public EmployeeDto createEmployee(
      @RequestPart EmployeeCreateRequest employeeCreateRequest,
      @RequestPart(value = "profile", required = false) MultipartFile profileImage
  ) throws IOException {
    return employeeService.createEmployee(employeeCreateRequest, profileImage);
  }

  @PatchMapping("/{id}")
  public EmployeeDto updateEmployee(@PathVariable long id,
      @RequestBody EmployeeUpdateRequest employeeUpdateRequest,
      @RequestPart(required = false) MultipartFile profileImage) throws IOException {
    return employeeService.updateEmployee(id, employeeUpdateRequest, profileImage);
  }

  @DeleteMapping("/{id}")
  public void deleteEmployee(@PathVariable long id) {
    employeeService.deleteEmployee(id);
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
    return employeeService.findEmployeeCount(request.status(), request.startDate(),
        request.endDate());
  }
}
