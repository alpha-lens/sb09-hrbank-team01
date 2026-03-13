package com.team1.hrbank.controller;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseDepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;
import com.team1.hrbank.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "부서 관리 API", description = "부서 등록, 목록/상세 조회, 수정, 삭제 기능을 제공합니다.")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

  private final DepartmentService departmentService;

  // 1. 부서 등록
  @Operation(summary = "부서 등록", description = "새로운 부서를 등록합니다. (이름 중복 불가)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "등록 성공",
          content = @Content(schema = @Schema(implementation = DepartmentDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이름",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class)))
  })
  @PostMapping
  public ResponseEntity<DepartmentDto> createDepartment(@RequestBody DepartmentCreateRequest request) {
    DepartmentDto response = departmentService.createDepartment(request);
    return ResponseEntity.ok(response);
  }

  // 2. 부서 수정
  @Operation(summary = "부서 수정", description = "부서 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "수정 성공",
          content = @Content(schema = @Schema(implementation = DepartmentDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 중복된 이름",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class)))
  })
  @PatchMapping("/{id}")
  public ResponseEntity<DepartmentDto> updateDepartment(
      @Parameter(description = "부서 ID") @PathVariable Long id,
      @RequestBody DepartmentUpdateRequest request) {
    DepartmentDto response = departmentService.updateDepartment(id, request);
    return ResponseEntity.ok(response);
  }

  // 3. 부서 상세 조회
  @Operation(summary = "부서 상세 조회", description = "특정 부서의 상세 정보와 현재 소속된 직원 수를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = DepartmentDto.class))),
      @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class)))
  })
  @GetMapping("/{id}")
  public ResponseEntity<DepartmentDto> findDepartment(
      @Parameter(description = "조회할 부서의 ID") @PathVariable Long id) {
    DepartmentDto response = departmentService.findDepartment(id);
    return ResponseEntity.ok(response);
  }

  // 4. 부서 목록 조회 (검색 기능 포함)
  @Operation(summary = "부서 목록 조회", description = "부서 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class)))
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseDepartmentDto> findAllDepartments(
      @Parameter(description = "부서 이름 또는 설명") @RequestParam(required = false) String nameOrDescription,
      @Parameter(description = "이전 페이지 마지막 요소 ID") @RequestParam(required = false) Long idAfter,
      @Parameter(description = "커서 (다음 페이지 시작점)") @RequestParam(required = false) String cursor,
      @Parameter(description = "페이지 크기 (기본값: 10)") @RequestParam(required = false, defaultValue = "10") Integer size,
      @Parameter(description = "정렬 필드 (name 또는 establishedDate)") @RequestParam(required = false, defaultValue = "establishedDate") String sortField,
      @Parameter(description = "정렬 방향 (asc 또는 desc, 기본값: asc)") @RequestParam(required = false, defaultValue = "asc") String sortDirection
  ) {
    // Service 로직을 수정 후 CursorPageResponseDepartmentDto 객체를 받아와야 함.(임시로 null 반환)
    return ResponseEntity.ok(null);
  }

  // 5. 부서 삭제
  @Operation(summary = "부서 삭제", description = "특정 부서를 삭제합니다. (소속 직원이 존재하는 경우 삭제 불가)")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "400", description = "소속 직원이 있는 부서는 삭제할 수 없음",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "404", description = "부서를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(implementation = com.team1.hrbank.global.GlobalExceptionHandler.ErrorResponse.class)))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteDepartment(
      @Parameter(description = "부서 ID") @PathVariable Long id) {
    departmentService.deleteDepartment(id);
    return ResponseEntity.noContent().build();
  }
}