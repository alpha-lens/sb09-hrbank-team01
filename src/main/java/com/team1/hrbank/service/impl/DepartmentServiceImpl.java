package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseDepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentSearchRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;
import com.team1.hrbank.entity.Department;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.repository.DepartmentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.service.DepartmentService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    if (request.name() != null
        && !request.name().equals(department.getName())) {
      if (departmentRepository.existsByName(request.name())) {
        throw new IllegalArgumentException("이미 존재하는 부서 이름입니다.");
      }
    }

    department.update(
        request.name(),
        request.description(),
        request.establishedDate()
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
  public CursorPageResponseDepartmentDto findAllDepartments(DepartmentSearchRequest request) {

    int limit = (request.size() != null && request.size() > 0) ? request.size() : 10;
    Pageable pageable = PageRequest.of(0, limit + 1);

    List<Department> departments = departmentRepository.findDepartmentsWithCursor(
        request.keyword(), request.cursor(), pageable
    );

    boolean hasNext = departments.size() > limit;
    List<Department> contentEntities = hasNext ? departments.subList(0, limit) : departments;

    long totalElements = (request.keyword() == null || request.keyword().trim().isEmpty())
        ? departmentRepository.count()
        : departmentRepository.countByKeyword(request.keyword());

    if (contentEntities.isEmpty()) {
      return new CursorPageResponseDepartmentDto(List.of(), null, 0L, limit, totalElements, false);
    }

    List<Long> departmentIds = contentEntities.stream().map(Department::getId).toList();
    List<Object[]> countResults = employeeRepository.countByDepartmentIds(departmentIds);
    Map<Long, Long> employeeCountMap = countResults.stream()
        .collect(Collectors.toMap(
            row -> (Long) row[0],
            row -> (Long) row[1]
        ));

    List<DepartmentDto> content = contentEntities.stream()
        .map(dept -> {
          long employeeCount = employeeCountMap.getOrDefault(dept.getId(), 0L);
          return toDto(dept, employeeCount);
        })
        .toList();

    long nextIdAfter = 0L;
    String nextCursor = null;
    if (hasNext) {
      Department lastItem = contentEntities.get(contentEntities.size() - 1);
      nextIdAfter = lastItem.getId();
      nextCursor = String.valueOf(nextIdAfter);
    }

    return new CursorPageResponseDepartmentDto(
        content, nextCursor, nextIdAfter, limit, totalElements, hasNext
    );
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
