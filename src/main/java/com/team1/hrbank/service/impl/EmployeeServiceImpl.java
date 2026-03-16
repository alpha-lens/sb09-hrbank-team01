package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeSearchRequest;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.entity.Department;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeDistribution;
import com.team1.hrbank.entity.EmployeeStatus;
import com.team1.hrbank.entity.EmployeeTrendTimeUnit;
import com.team1.hrbank.repository.BinaryContentRepository;
import com.team1.hrbank.repository.DepartmentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.repository.projection.DistributionMapping;
import com.team1.hrbank.repository.projection.EmployeeTrendMapping;
import com.team1.hrbank.service.EmployeeService;
import com.team1.hrbank.specification.EmployeeSpecification;
import com.team1.hrbank.storage.FileStorageService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

  private final EmployeeRepository employeeRepository;
  private final DepartmentRepository departmentRepository;
  private final FileStorageService fileStorageService;
  private final BinaryContentRepository binaryContentRepository;

  public String generateEmployeeNumber(String prefix, int lastSequence) {
    int nextSequence = lastSequence + 1;
    return String.format("%s-%03d", prefix, nextSequence);
  }

  private EmployeeDto toDto(Employee entity) {
    return new EmployeeDto(
        entity.getId(),
        entity.getName(),
        entity.getEmail(),
        entity.getEmployeeNumber(),
        entity.getDepartment().getId(),
        entity.getDepartment().getName(),
        entity.getPosition(),
        entity.getHireDate(),
        entity.getStatus(),
        entity.getProfileImage() == null ? null : entity.getProfileImage().getId()
    );
  }

  private String formatDate(LocalDate date, EmployeeTrendTimeUnit unit) {
    return switch (unit) {
      case DAY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      case MONTH -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
      case YEAR -> date.format(DateTimeFormatter.ofPattern("yyyy"));
      case QUARTER -> null;
      case WEEK -> null;
    };
  }

  private LocalDate incrementDate(LocalDate date, EmployeeTrendTimeUnit unit) {
    return switch (unit) {
      case DAY -> date.plusDays(1);
      case MONTH -> date.plusMonths(1);
      case YEAR -> date.plusYears(1);
      case QUARTER -> null;
      case WEEK -> null;
    };
  }

  private BinaryContent saveProfileImage(MultipartFile profileImage) {
    String fileName = profileImage.getOriginalFilename();
    String contentType = profileImage.getContentType();
    Long size = profileImage.getSize();
    String storedFileName = UUID.randomUUID() + "_" + fileName;
    String filePath = "./file-data-map/" + storedFileName;

    return new BinaryContent(fileName, contentType, size, filePath);
  }

  @Override
  @Transactional
  public EmployeeDto createEmployee(EmployeeCreateRequest request, MultipartFile profileImage)
      throws IOException {
    if (employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
    }

    String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    String lastEmployeeNumber = employeeRepository.findLastEmployeeNameByPrefix(prefix);
    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new NoSuchElementException("해당 부서를 찾을 수 없습니다."));
    String employeeNumber = lastEmployeeNumber != null ?
        generateEmployeeNumber(prefix,Integer.parseInt(lastEmployeeNumber.substring(7)))
        : generateEmployeeNumber(prefix, 1);

    Employee employee = Employee.of(employeeNumber, request.name(), request.email(), department,
        request.position());

    if(profileImage != null && !profileImage.isEmpty()) {
      BinaryContent profile = saveProfileImage(profileImage);
      binaryContentRepository.save(profile);
      fileStorageService.save(profile.getId(), profileImage.getBytes());
      employee.updateProfileImage(profile);
    }

    return toDto(employeeRepository.save(employee));
  }

  @Override
  @Transactional
  public EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest request, MultipartFile profileImage)
      throws IOException {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다 : " + id));
    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new NoSuchElementException("해당 부서를 찾을 수 없습니다 : " + request.departmentId()));

    employee.update(
        request.name(), request.email(), department, request.position(), request.hireDate(),
        request.status()
    );

    if(profileImage != null && !profileImage.isEmpty()) {
      BinaryContent profile = saveProfileImage(profileImage);
      binaryContentRepository.save(profile);
      fileStorageService.save(profile.getId(), profileImage.getBytes());
      employee.updateProfileImage(profile);
    }

    return toDto(employee);
  }

  @Override
  public EmployeeDto findEmployee(Long id) {
    return toDto(employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다 : " + id)));
  }
  @Override
  public List<EmployeeDto> findAllEmployees(EmployeeSearchRequest request) {
    // 1. 클라이언트의 sortField를 엔티티 경로로 변환
    String sortField = request.sortField();
    if (sortField == null || sortField.isBlank()) {
      sortField = "id"; // 기본값
    } else if ("departmentName".equals(sortField)) {
      // 부서 이름으로 정렬 요청이 오면 'department' 엔티티의 'name' 필드를 보도록 수정
      sortField = "department.name";
    }

    Sort.Direction direction = "desc".equalsIgnoreCase(request.sortDirection())
        ? Sort.Direction.DESC : Sort.Direction.ASC;

    // 2. Pageable 객체 생성
    Pageable pageable = PageRequest.of(0, request.size(), Sort.by(direction, sortField));

    // 3. Specification과 Pageable을 함께 사용하여 조회
    Page<Employee> employeePage = employeeRepository.findAll(
        EmployeeSpecification.filterBy(request),
        pageable
    );

    // 4. DTO로 변환하여 반환
    return employeePage.getContent().stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public void deleteEmployee(Long id) {
    if (!employeeRepository.existsById(id)) {
      throw new NoSuchElementException("해당 직원을 찾을 수 없습니다: " + id);
    }

    employeeRepository.deleteById(id);
  }

  @Override
  public List<EmployeeTrendDto> findEmployeeTrend(LocalDate startDate, LocalDate endDate,
      EmployeeTrendTimeUnit unit) {
    // 1. 기본값 및 기간 설정
    LocalDate finalEnd = (endDate != null) ? endDate : LocalDate.now();
    EmployeeTrendTimeUnit finalUnit = (unit != null) ? unit : EmployeeTrendTimeUnit.MONTH;
    LocalDate finalStart =
        (startDate != null) ? startDate : finalEnd.minusMonths(11).withDayOfMonth(1);

    // 2. DB 데이터 조회 (인터페이스로 받음)
    List<EmployeeTrendMapping> rawResults = employeeRepository.getTrendData(
        finalStart, finalEnd, finalUnit.name()
    );

    // 3. 조회를 위해 Map으로 변환 (Key: 날짜문자열, Value: 카운트)
    Map<String, Integer> dbDataMap = rawResults.stream()
        .collect(Collectors.toMap(EmployeeTrendMapping::getPeriod, EmployeeTrendMapping::getCount));

    // 4. 전체 기간 루프 돌며 0 채우기 및 DTO 생성
    List<EmployeeTrendDto> result = new ArrayList<>();
    LocalDate current = finalStart;
    int previousCount = 0;

    while (!current.isAfter(finalEnd)) {
      String dateKey = formatDate(current, finalUnit);
      int currentCount = dbDataMap.getOrDefault(dateKey, 0);

      // 변동치 계산
      int change = currentCount - previousCount;
      double changeRate = (previousCount == 0) ? (currentCount > 0 ? 100.0 : 0.0)
          : ((double) change / previousCount) * 100;

      result.add(new EmployeeTrendDto(
          dateKey,
          currentCount,
          change,
          Math.round(changeRate * 100) / 100.0
      ));

      previousCount = currentCount;
      current = incrementDate(current, finalUnit);
    }

    return result;
  }

  @Override
  public List<EmployeeDistributionDto> findEmployeeDistribution(LocalDate startDate,
      LocalDate endDate,
      EmployeeDistribution distribution, EmployeeStatus status) {

    List<DistributionMapping> rawData = switch (distribution) {
      case DEPARTMENT -> employeeRepository.findDistributionByDepartment(status.name());
      case POSITION -> employeeRepository.findDistributionByPosition(status.name());
    };

    long totalCount = rawData.stream()
        .mapToLong(DistributionMapping::getCount)
        .sum();

    // 3. DTO 변환 및 비율 계산
    return rawData.stream()
        .map(data -> {
          double percent = (totalCount == 0) ? 0.0 : (double) data.getCount() / totalCount * 100;
          double roundedPercent = Math.round(percent * 10.0) / 10.0;

          return new EmployeeDistributionDto(data.getLabel(), data.getCount(), roundedPercent);
        })
        .toList();
  }

  @Override
  public long findEmployeeCount(EmployeeStatus status, LocalDate startDate, LocalDate endDate) {
    if (endDate == null) {
      endDate = LocalDate.now();
    }

    if (startDate == null) {
      startDate = endDate.minusWeeks(1);
    }
    return employeeRepository.findEmployeeCount(status, startDate, endDate);
  }
}
