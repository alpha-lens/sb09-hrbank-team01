package com.team1.hrbank.service;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.cursor.CursorPageResponse;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentSearchRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;

public interface DepartmentService {

  DepartmentDto createDepartment(DepartmentCreateRequest request);

  DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request);

  DepartmentDto findDepartment(Long id);

  CursorPageResponse<DepartmentDto> findAllDepartments(DepartmentSearchRequest request);

  void deleteDepartment(Long id);

}
