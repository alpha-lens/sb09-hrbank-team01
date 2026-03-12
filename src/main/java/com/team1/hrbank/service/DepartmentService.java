package com.team1.hrbank.service;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;
import java.util.List;

public interface DepartmentService {

  DepartmentDto createDepartment(DepartmentCreateRequest request);

  DepartmentDto updateDepartment(DepartmentUpdateRequest request);

  DepartmentDto findDepartment(Long id);

  List<DepartmentDto> findAllDepartments();

  void deleteDepartment(Long id);

}
