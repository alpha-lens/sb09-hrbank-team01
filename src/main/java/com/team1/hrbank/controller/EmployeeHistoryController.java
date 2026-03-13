package com.team1.hrbank.controller;

import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.entity.HistoryType;
import com.team1.hrbank.service.EmployeeHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
  @ApiResponse(responseCode = "200", description = "직원 정보 수정 이력 성공")
  @PostMapping
  public ResponseEntity<Void> createEmployeeHistory(
      @RequestBody EmployeeHistoryCreateRequest request,
      HttpServletRequest httpRequest  // IP 추출용
  ) {
    // X-Forwarded-For 헤더 우선 확인 (프록시 환경)
    // 없으면 직접 연결된 클라이언트 IP 사용
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
  public ResponseEntity<CursorPageResponseEmployeeHistoryDto> findEmployeeHistories(
      // 검색 조건 (모두 선택적)
      @RequestParam(required = false) String employeeNumber,
      @RequestParam(required = false) HistoryType type,
      @RequestParam(required = false) String memo,
      @RequestParam(required = false) String ipAddress,
      @RequestParam(required = false) Instant atFrom,
      @RequestParam(required = false) Instant atTo,
      // 커서 페이지네이션
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "10") int size,
      // 정렬
      @RequestParam(defaultValue = "at") String sortField,
      @RequestParam(defaultValue = "desc") String sortDirection
  ) {
    return ResponseEntity.ok(
        employeeHistoryService.findEmployeeHistories(
            employeeNumber, type, memo, ipAddress,
            atFrom, atTo, idAfter, cursor,
            size, sortField, sortDirection
        )
    );
  }

  @Operation(summary = "수정 이력 건수 조회")
  @ApiResponse(responseCode = "200", description = "조회")
  @GetMapping("/{count}")
  public ResponseEntity<List<EmployeeHistoryDto>> findEmployeeHistoriesByRevisionsBetween(
      @RequestParam(required = false) Long fromRevision,
      @RequestParam(required = false) Long toRevision
  ) {
    return ResponseEntity.ok(employeeHistoryService.findEmployeeHistoriesByRevisionsBetween(fromRevision, toRevision));
  }

  @GetMapping
  public ResponseEntity<List<EmployeeHistoryDto>> findAllEmployeeHistories() {
    return ResponseEntity.ok(employeeHistoryService.findAllEmployeeHistories());
  }

}
