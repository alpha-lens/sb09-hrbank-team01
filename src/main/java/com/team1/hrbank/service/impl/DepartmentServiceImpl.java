package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;
import com.team1.hrbank.entity.Department;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.repository.DepartmentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.service.DepartmentService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final EmployeeRepository employeeRepository;

  @Override
  @Transactional
  public DepartmentDto createDepartment(DepartmentCreateRequest request) {
    if (departmentRepository.existsByName(request.name())) {
      throw new IllegalArgumentException("이미 존재하는 부서 이름이에요");
    }

    Department department = Department.of(
        request.name(),
        request.description(),
        request.establishedDate()
    );

    Department saved = departmentRepository.save(department);
    return toDto(saved, 0L); // 방금 생성했으니 직원 수는 0
  }

  @Override
  @Transactional
  public DepartmentDto updateDepartment(Long id, DepartmentUpdateRequest request) {
    Department department = getDepartmentOrThrow(id);

    if (request.newName() != null
        && !request.newName().equals(department.getName())) {
      if (departmentRepository.existsByName(request.newName())) {
        throw new IllegalArgumentException("이미 존재하는 부서 이름입니다.");
      }
    }

    department.update(
        request.newName(),
        request.newDescription(),
        request.newEstablishedDate()
    );

    long employeeCount = employeeRepository.countByDepartmentId(department.getId());
    return toDto(department, employeeCount);
  }

  @Override
  public DepartmentDto findDepartment(Long id) {
    Department department = getDepartmentOrThrow(id);
    long employeeCount = employeeRepository.countByDepartmentId(id);
    return toDto(department, employeeCount);
  }

  @Override
  public List<DepartmentDto> findAllDepartments(String keyword) {
    List<Department> departments;

    if (keyword == null || keyword.trim().isEmpty()) {
      departments = departmentRepository.findAll();
    }  else {
      departments = departmentRepository.searchByKeyword(keyword);
    }

    // 검색된 부서가 없으면 빈 리스트 반환
    if (departments.isEmpty()) {
      return List.of();
    }

    List<Long> departmentIds = departments.stream()
        .map(Department::getId)
        .toList();

    List<Object[]> countResults = employeeRepository.countByDepartmentIds(departmentIds);

    Map<Long, Long> employeeCountMap = countResults.stream()
        .collect(java.util.stream.Collectors.toMap(
            row -> (Long) row[0],
            row -> (Long) row[1]
        ));

    return departments.stream()
        .map(dept -> {
          long employeeCount = employeeCountMap.getOrDefault(dept.getId(), 0L);
          return toDto(dept, employeeCount);
        })
        .toList();
  }

  @Override
  @Transactional
  public void deleteDepartment(Long id) {
    Department department = getDepartmentOrThrow(id);

    if (employeeRepository.existsByDepartmentId(id)) {
      throw new IllegalArgumentException("소속 직원이 있는 부서는 삭제할 수 없습니다.");
    }

    departmentRepository.delete(department);
  }

  private Department getDepartmentOrThrow(Long id) {
    return departmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("부서를 찾을 수 없습니다."));
  }

  private DepartmentDto toDto(Department department, long employeeCount) {
    return new DepartmentDto(
        department.getId(),
        department.getName(),
        department.getDescription(),
        department.getEstablishedDate().toString(),
        employeeCount
    );
  }

}
