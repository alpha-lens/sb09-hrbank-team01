package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.dto.request.EmployeeHistorySearchRequest;
import com.team1.hrbank.service.EmployeeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/change-logs")
@RequiredArgsConstructor
public class EmployeeHistoryController {

  private final EmployeeHistoryService employeeHistoryService;

  @Operation(summary = "직원 정보 수정 이력 생성")
  @ApiResponse(responseCode = "201", description = "직원 정보 수정 이력 생성 성공")
  @PostMapping
  public ResponseEntity<Void> createEmployeeHistory(
      @RequestBody EmployeeHistoryCreateRequest request,
      HttpServletRequest httpRequest
  ) {
    String ipAddress = httpRequest.getHeader("X-Forwarded-For");
    if (ipAddress == null) {
      ipAddress = httpRequest.getRemoteAddr();
    }
    employeeHistoryService.createEmployeeHistory(request, ipAddress);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Operation(summary = "직원 정보 수정 이력 조회")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청입니다")
      }
  )
  @GetMapping
  public ResponseEntity<CursorPageResponseEmployeeHistoryDto> findEmployeeHistories(@ModelAttribute
    EmployeeHistorySearchRequest request) {
    return ResponseEntity.ok(
        employeeHistoryService.findEmployeeHistories(request)
    );
  }

  @Operation(summary = "직원 정보 수정 이력 상세 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "404", description = "이력을 찾을 수 없습니다")
  })
  @GetMapping("/{id}")
  public ResponseEntity<EmployeeHistoryDetailDto> findEmployeeHistory(
      @PathVariable Long id
  ) {
    return ResponseEntity.ok(employeeHistoryService.findEmployeeHistory(id));
  }

  @Operation(summary = "수정 이력 건수 조회")
  @ApiResponses(
      value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청입니다")
      }
  )
  @GetMapping("/count")
  public ResponseEntity<Long> findEmployeeHistoriesByRevisionsBetween(
      @RequestParam(required = false) Instant fromDate,
      @RequestParam(required = false) Instant toDate
  ) {
    return ResponseEntity.ok(
        employeeHistoryService.countEmployeeHistories(fromDate, toDate)
    );
  }

}
