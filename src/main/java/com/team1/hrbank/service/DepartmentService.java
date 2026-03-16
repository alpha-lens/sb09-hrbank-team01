package com.team1.hrbank.service;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseDepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;

public interface DepartmentService {

  DepartmentDto createDepartment(DepartmentCreateRequest request);

  DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request);

  DepartmentDto findDepartment(Long id);

  CursorPageResponseDepartmentDto findAllDepartments(
      String keyword, Long idAfter, String cursor, Integer size, String sortField, String sortDirection
  );

  void deleteDepartment(Long id);

}
