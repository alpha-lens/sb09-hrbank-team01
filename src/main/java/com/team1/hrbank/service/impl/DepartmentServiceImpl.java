package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.cursor.CursorPageResponse;
import com.team1.hrbank.dto.DepartmentDto;
import com.team1.hrbank.dto.request.DepartmentCreateRequest;
import com.team1.hrbank.dto.request.DepartmentSearchRequest;
import com.team1.hrbank.dto.request.DepartmentUpdateRequest;
import com.team1.hrbank.entity.Department;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeStatus;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.repository.DepartmentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.service.DepartmentService;
import com.team1.hrbank.specification.DepartmentSpecification;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
      throw new IllegalArgumentException("IllegalArgumentException");
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
        throw new IllegalArgumentException("IllegalArgumentException");
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
  public CursorPageResponse<DepartmentDto> findAllDepartments(DepartmentSearchRequest request) {

    int limit = (request.size() != null && request.size() > 0) ? request.size() : 10;
    // 1. 정렬 기준 세팅
    String sortField = (request.sortField() != null && !request.sortField().trim().isEmpty()) ? request.sortField() : "id";
    Sort.Direction direction = "desc".equalsIgnoreCase(request.sortDirection()) ? Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

    Pageable pageable = PageRequest.of(0, limit + 1, sort);

    // 2. 기준이 될 커서 부서 가져오기
    Department cursorDept = null;
    if (request.cursor() != null && request.cursor() > 0) {
      cursorDept = departmentRepository.findById(request.cursor()).orElse(null);
    }

    // 3. Specification 사용해서 조회
    List<Department> departments = departmentRepository.findAll(
        DepartmentSpecification.filterBy(request, cursorDept), pageable
    ).getContent();

    // 4. hasNext 판단 및 자르기
    boolean hasNext = departments.size() > limit;
    List<Department> contentEntities = hasNext ? departments.subList(0, limit) : departments;

    long totalElements = departmentRepository.count(DepartmentSpecification.filterBy(request, null)
    );

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

    return CursorPageResponse.of(
        content, limit, totalElements, DepartmentDto::id
    );
  }

  @Override
  @Transactional
  public void deleteDepartment(Long id) {
    Department department = getDepartmentOrThrow(id);

    if (employeeRepository.existsByDepartmentIdAndNotResigned(id)) {
      throw new IllegalStateException("소속 직원이 있는 부서는 삭제할 수 없습니다.");
    }

    // RESIGNED 직원은 부서 삭제 전에 함께 삭제
    List<Employee> resignedEmployees = employeeRepository.findByDepartmentIdAndStatus(id, EmployeeStatus.RESIGNED);
    if (!resignedEmployees.isEmpty()) {
      employeeRepository.deleteAll(resignedEmployees);
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
