package com.team1.hrbank.service;

import com.team1.hrbank.entity.Department;
import java.time.LocalDate;
import java.util.List;

public interface DepartmentService {
  public void createDepartment(DepartmentCreateRequest request);

  public void updateDepartment(DepartmentUpdateRequest request);

  public Department findDepartment(Long id);

  public List<Department> findAllDepartments();

  public void deleteDepartment(Long id);

}
