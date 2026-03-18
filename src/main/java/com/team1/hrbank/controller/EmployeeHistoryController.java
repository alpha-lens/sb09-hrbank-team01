package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.cursor.CursorPageResponse;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.dto.request.EmployeeHistorySearchRequest;
import com.team1.hrbank.service.EmployeeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
    String ipAddress = resolveIpAddress(httpRequest);
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
  public ResponseEntity<CursorPageResponse<EmployeeHistoryDto>> findEmployeeHistories(
      @ParameterObject @ModelAttribute EmployeeHistorySearchRequest request) {
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
      @RequestParam(required = false) String fromDate,
      @RequestParam(required = false) String toDate
  ) {
    return ResponseEntity.ok(
        employeeHistoryService.countEmployeeHistories(
            parseInstant(fromDate), parseInstant(toDate))
    );
  }

  private String resolveIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (ip != null && !ip.isBlank()) {
      ip = ip.split(",")[0].trim();
    } else {
      ip = request.getRemoteAddr();
    }
    if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "127.0.0.1".equals(ip)) {
      ip = getLocalIp();
    }
    return ip;
  }

  private String getLocalIp() {
    try {
      java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        java.net.NetworkInterface ni = interfaces.nextElement();
        if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
        java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
        while (addresses.hasMoreElements()) {
          java.net.InetAddress addr = addresses.nextElement();
          if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (java.net.SocketException e) {
      // ignore
    }
    return "127.0.0.1";
  }

  private Instant parseInstant(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) {
      return null;
    }
    try {
      return Instant.parse(dateStr);
    } catch (Exception e) {
      return LocalDateTime.parse(dateStr).toInstant(ZoneOffset.UTC);
    }
  }

}
