package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.dto.EmployeeDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeDto;
import com.team1.hrbank.dto.dashboard.EmployeeDistributionDto;
import com.team1.hrbank.dto.dashboard.EmployeeTrendDto;
import com.team1.hrbank.dto.request.EmployeeCreateRequest;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.dto.request.EmployeeSearchRequest;
import com.team1.hrbank.dto.request.EmployeeUpdateRequest;
import com.team1.hrbank.entity.BinaryContent;
import com.team1.hrbank.entity.Department;
import com.team1.hrbank.entity.Employee;
import com.team1.hrbank.entity.EmployeeDistribution;
import com.team1.hrbank.entity.EmployeeStatus;
import com.team1.hrbank.entity.EmployeeTrendTimeUnit;
import com.team1.hrbank.entity.HistoryType;
import com.team1.hrbank.repository.BinaryContentRepository;
import com.team1.hrbank.repository.DepartmentRepository;
import com.team1.hrbank.repository.EmployeeRepository;
import com.team1.hrbank.repository.projection.DistributionMapping;
import com.team1.hrbank.repository.projection.EmployeeTrendMapping;
import com.team1.hrbank.service.EmployeeHistoryService;
import com.team1.hrbank.service.EmployeeService;
import com.team1.hrbank.specification.EmployeeSpecification;
import com.team1.hrbank.storage.FileStorageService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
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
  private final EmployeeHistoryService employeeHistoryService;

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
      case QUARTER -> {
        int year = date.getYear();
        int quarter = date.get(IsoFields.QUARTER_OF_YEAR);
        yield String.format("%d-Q%d", year, quarter);
      }
      case WEEK -> {
        int year = date.get(IsoFields.WEEK_BASED_YEAR);
        int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        yield String.format("%d-W%02d", year, week);
      }
    };
  }

  private LocalDate incrementDate(LocalDate date, EmployeeTrendTimeUnit unit) {
    return switch (unit) {
      case DAY -> date.plusDays(1);
      case MONTH -> date.plusMonths(1);
      case YEAR -> date.plusYears(1);
      case QUARTER -> date.plusMonths(3);
      case WEEK -> date.plusWeeks(1);
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
  public EmployeeDto createEmployee(EmployeeCreateRequest request, MultipartFile profileImage, String ipAddress)
      throws IOException {
    if (request.name() == null || request.name().isBlank()) {
      throw new IllegalArgumentException("이름은 필수 입력값입니다.");
    }
    if (request.email() == null || request.email().isBlank()) {
      throw new IllegalArgumentException("이메일은 필수 입력값입니다.");
    }
    if (request.departmentId() == null) {
      throw new IllegalArgumentException("부서 ID는 필수 입력값입니다.");
    }
    if (request.position() == null || request.position().isBlank()) {
      throw new IllegalArgumentException("직함은 필수 입력값입니다.");
    }
    if (request.hireDate() == null) {
      throw new IllegalArgumentException("입사일은 필수 입력값입니다.");
    }

    if (employeeRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
    }

    String prefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    String lastEmployeeNumber = employeeRepository.findLastEmployeeNameByPrefix(prefix);
    Department department = departmentRepository.findById(request.departmentId())
        .orElseThrow(() -> new NoSuchElementException("해당 부서를 찾을 수 없습니다."));
    String employeeNumber = lastEmployeeNumber != null ?
        generateEmployeeNumber(prefix, Integer.parseInt(lastEmployeeNumber.substring(7)))
        : generateEmployeeNumber(prefix, 0);

    Employee employee = Employee.of(employeeNumber, request.name(), request.email(), department,
        request.position(), request.hireDate());

    if (profileImage != null && !profileImage.isEmpty()) {
      BinaryContent profile = saveProfileImage(profileImage);
      binaryContentRepository.save(profile);
      fileStorageService.save(profile.getId(), profileImage.getBytes());
      employee.updateProfileImage(profile);
    }

    Employee saved = employeeRepository.save(employee);

    // 변경 이력 자동 생성
    List<DiffDto> diffs = List.of(
        new DiffDto("employeeNumber", "", saved.getEmployeeNumber()),
        new DiffDto("name", "", saved.getName()),
        new DiffDto("email", "", saved.getEmail()),
        new DiffDto("department", "", department.getName()),
        new DiffDto("position", "", saved.getPosition()),
        new DiffDto("hireDate", "", saved.getHireDate().toString()),
        new DiffDto("status", "", saved.getStatus().name())
    );
    employeeHistoryService.createEmployeeHistory(
        new EmployeeHistoryCreateRequest(HistoryType.CREATED, saved.getEmployeeNumber(), diffs, request.memo()),
        ipAddress
    );

    return toDto(saved);
  }

  @Override
  @Transactional
  public EmployeeDto updateEmployee(Long id, EmployeeUpdateRequest request,
      MultipartFile profileImage, String ipAddress)
      throws IOException {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다 : " + id));

    // 변경 전 값 저장
    String beforeName = employee.getName();
    String beforeEmail = employee.getEmail();
    String beforeDepartmentName = employee.getDepartment().getName();
    Long beforeDepartmentId = employee.getDepartment().getId();
    String beforePosition = employee.getPosition();
    String beforeHireDate = employee.getHireDate().toString();
    String beforeStatus = employee.getStatus().name();

    Department department = null;
    if (request.departmentId() != null) {
      department = departmentRepository.findById(request.departmentId())
          .orElseThrow(
              () -> new NoSuchElementException("해당 부서를 찾을 수 없습니다 : " + request.departmentId()));
    }

    employee.update(
        request.name(), request.email(), department, request.position(), request.hireDate(),
        request.status()
    );

    if (profileImage != null && !profileImage.isEmpty()) {
      if (employee.getProfileImage() != null) {
        fileStorageService.delete(employee.getProfileImage().getId());
        binaryContentRepository.delete(employee.getProfileImage());
      }

      BinaryContent profile = saveProfileImage(profileImage);
      binaryContentRepository.save(profile);
      fileStorageService.save(profile.getId(), profileImage.getBytes());
      employee.updateProfileImage(profile);
    }

    // 변경 이력 자동 생성
    List<DiffDto> diffs = new ArrayList<>();
    if (request.name() != null && !request.name().equals(beforeName)) {
      diffs.add(new DiffDto("name", beforeName, request.name()));
    }
    if (request.email() != null && !request.email().equals(beforeEmail)) {
      diffs.add(new DiffDto("email", beforeEmail, request.email()));
    }
    if (request.departmentId() != null && !request.departmentId().equals(beforeDepartmentId)) {
      diffs.add(new DiffDto("department", beforeDepartmentName, employee.getDepartment().getName()));
    }
    if (request.position() != null && !request.position().equals(beforePosition)) {
      diffs.add(new DiffDto("position", beforePosition, request.position()));
    }
    if (request.hireDate() != null && !request.hireDate().toString().equals(beforeHireDate)) {
      diffs.add(new DiffDto("hireDate", beforeHireDate, request.hireDate().toString()));
    }
    if (request.status() != null && !request.status().name().equals(beforeStatus)) {
      diffs.add(new DiffDto("status", beforeStatus, request.status().name()));
    }

    if (!diffs.isEmpty()) {
      employeeHistoryService.createEmployeeHistory(
          new EmployeeHistoryCreateRequest(HistoryType.UPDATED, employee.getEmployeeNumber(), diffs, request.memo()),
          ipAddress
      );
    }

    return toDto(employee);
  }

  @Override
  public EmployeeDto findEmployee(Long id) {
    return toDto(employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다 : " + id)));
  }

  @Override
  public CursorPageResponseEmployeeDto findAllEmployees(EmployeeSearchRequest request) {
    // BUG-3 수정: sortField/sortDirection 적용 (기본값: id ASC)
    String sortField = (request.sortField() != null && !request.sortField().isBlank())
        ? request.sortField() : "id";
    Direction direction = "desc".equalsIgnoreCase(request.sortDirection())
        ? Direction.DESC : Direction.ASC;
    Sort sort = Sort.by(direction, sortField);
    if (!"id".equals(sortField)) {
      sort = sort.and(Sort.by(direction, "id"));
    }
    Pageable pageable = PageRequest.of(0, request.size() + 1, sort);

    Page<Employee> employeePage = employeeRepository.findAll(
        EmployeeSpecification.filterBy(request),
        pageable
    );

    List<Employee> content = employeePage.getContent();

    boolean hasNext = content.size() > request.size();
    List<Employee> resultList = hasNext ? content.subList(0, request.size()) : content;

    // BUG-4 수정: hasNext=true일 때만 nextCursor/nextIdAfter 설정
    String nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !resultList.isEmpty()) {
      Long lastId = resultList.get(resultList.size() - 1).getId();
      nextCursor = String.valueOf(lastId);
      nextIdAfter = lastId;
    }

    List<EmployeeDto> dtoList = resultList.stream().map(this::toDto).toList();

    // BUG-2 수정: 커서 조건 제외한 카운트 (전체 검색 결과 수)
    long totalElements = employeeRepository.count(EmployeeSpecification.filterForCount(request));

    return new CursorPageResponseEmployeeDto(
        dtoList,
        nextCursor,
        nextIdAfter,
        request.size(),
        totalElements,
        hasNext
    );
  }

  @Override
  @Transactional
  public void deleteEmployee(Long id, String ipAddress) {
    Employee employee = employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다: " + id));

    // 삭제 전 이력 생성
    List<DiffDto> diffs = List.of(
        new DiffDto("employeeNumber", employee.getEmployeeNumber(), ""),
        new DiffDto("name", employee.getName(), ""),
        new DiffDto("email", employee.getEmail(), ""),
        new DiffDto("department", employee.getDepartment().getName(), ""),
        new DiffDto("position", employee.getPosition(), ""),
        new DiffDto("hireDate", employee.getHireDate().toString(), ""),
        new DiffDto("status", employee.getStatus().name(), "")
    );
    employeeHistoryService.createEmployeeHistory(
        new EmployeeHistoryCreateRequest(HistoryType.DELETED, employee.getEmployeeNumber(), diffs, null),
        ipAddress
    );

    Long profileImageId = employee.getProfileImage() != null ? employee.getProfileImage().getId() : null;

    employeeRepository.delete(employee);

    if (profileImageId != null && fileStorageService.exists(profileImageId)) {
      fileStorageService.delete(profileImageId);
    }
  }

  @Override
  public List<EmployeeTrendDto> findEmployeeTrend(LocalDate startDate, LocalDate endDate,
      EmployeeTrendTimeUnit unit) {
    // 1. 기본값 및 기간 설정
    LocalDate finalEnd = (endDate != null) ? endDate : LocalDate.now();
    EmployeeTrendTimeUnit finalUnit = (unit != null) ? unit : EmployeeTrendTimeUnit.MONTH;
    LocalDate finalStart;
    if (startDate != null) {
      finalStart = startDate;
    } else if (finalUnit == EmployeeTrendTimeUnit.YEAR) {
      finalStart = finalEnd.minusYears(4).withDayOfYear(1);
    } else {
      finalStart = finalEnd.minusMonths(11).withDayOfMonth(1);
    }

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

    List<DistributionMapping> rawData = null;
    if (status == null) {
      status = EmployeeStatus.ACTIVE;
    }

    if (distribution == EmployeeDistribution.POSITION) {
      rawData = employeeRepository.findDistributionByPosition(status.name());
    } else {
      rawData = employeeRepository.findDistributionByDepartment(status.name());
    }

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
      startDate = LocalDate.EPOCH;
    }

    if (status == null) {
      return employeeRepository.findEmployeeCountAll(startDate, endDate);
    }
    return employeeRepository.findEmployeeCount(status.name(), startDate, endDate);
  }
}
