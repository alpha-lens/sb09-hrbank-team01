package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.dto.cursor.CursorPageResponse;
import com.team1.hrbank.dto.EmployeeDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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

  private List<DiffDto> buildDiffs(Employee before, EmployeeUpdateRequest request) {
    List<DiffDto> diffs = new ArrayList<>();
    if (request.name() != null && !request.name().equals(before.getName())) {
      diffs.add(new DiffDto("name", before.getName(), request.name()));
    }
    if (request.email() != null && !request.email().equals(before.getEmail())) {
      diffs.add(new DiffDto("email", before.getEmail(), request.email()));
    }
    if (request.departmentId() != null && !request.departmentId().equals(before.getDepartment().getId())) {
      diffs.add(new DiffDto("departmentId",
          String.valueOf(before.getDepartment().getId()),
          String.valueOf(request.departmentId())));
    }
    if (request.position() != null && !request.position().equals(before.getPosition())) {
      diffs.add(new DiffDto("position", before.getPosition(), request.position()));
    }
    if (request.hireDate() != null && !request.hireDate().equals(before.getHireDate())) {
      diffs.add(new DiffDto("hireDate",
          before.getHireDate().toString(),
          request.hireDate().toString()));
    }
    if (request.status() != null && !request.status().equals(before.getStatus())) {
      diffs.add(new DiffDto("status",
          before.getStatus().name(),
          request.status().name()));
    }
    return diffs;
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

    EmployeeDto saved = toDto(employeeRepository.save(employee));

    employeeHistoryService.createEmployeeHistory(
        new EmployeeHistoryCreateRequest(HistoryType.CREATED, saved.employeeNumber(), List.of(), request.memo()),
        ipAddress
    );

    return saved;
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

    List<DiffDto> diffs = buildDiffs(employee,request);

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

    employeeHistoryService.createEmployeeHistory(
        new EmployeeHistoryCreateRequest(HistoryType.UPDATED, employee.getEmployeeNumber(), diffs, request.memo()),
        ipAddress
    );

    return toDto(employee);
  }

  @Override
  public EmployeeDto findEmployee(Long id) {
    return toDto(employeeRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 직원을 찾을 수 없습니다 : " + id)));
  }

  @Override
  public CursorPageResponse findAllEmployees(EmployeeSearchRequest request) {
    String sortField = StringUtils.hasText(request.sortField()) ? request.sortField() : "id";
    Direction direction = request.sortDirection().equalsIgnoreCase("DESC") ? Direction.DESC : Direction.ASC;
    Sort sort = Sort.by(direction, sortField).and(Sort.by(direction, "id"));

    Pageable pageable = PageRequest.of(0, request.size() + 1, sort);

    // 2. 실제 데이터 조회 (카운트 쿼리도 함께 발생하지만 idAfter 조건은 무시되도록 Specification에 설정됨)
    List<Employee> content = employeeRepository.findAll(
        EmployeeSpecification.filterBy(request),
        pageable
    ).getContent();

    // 3. 다음 페이지 존재 여부 확인 및 데이터 자르기
    boolean hasNext = content.size() > request.size();
    List<Employee> resultList = hasNext ? content.subList(0, request.size()) : content;

    // BUG-4 수정: hasNext=true일 때만 nextCursor/nextIdAfter 설정
    String nextCursor = null;
    Long nextIdAfter = 0L;

    if (!resultList.isEmpty()) {
      Employee lastEmp = resultList.get(resultList.size() - 1);
      nextIdAfter = lastEmp.getId();

      // 정렬 필드에 따라 cursor에 담을 값 결정
      nextCursor = switch (sortField) {
        case "name" -> lastEmp.getName() + "_" + nextIdAfter;
        case "hireDate" -> lastEmp.getHireDate().toString() + "_" + nextIdAfter;
        case "position" -> lastEmp.getPosition() + "_" + nextIdAfter;
        case "employeeNumber" -> lastEmp.getEmployeeNumber() + "_" + nextIdAfter;
        default -> String.valueOf(lastEmp.getId());
      };
    }

    List<EmployeeDto> dtoList = resultList.stream().map(this::toDto).toList();

    // 4. 전체 데이터 수 조회 (커서 조건 없이 순수 검색 조건으로만 카운트)
    long totalElements = employeeRepository.count(EmployeeSpecification.filterBy(request));

    return new CursorPageResponse(
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

    Long profileId = (employee.getProfileImage() != null) ?
        employee.getProfileImage().getId() : null;
    employeeRepository.delete(employee);

    String empNumber = employee.getEmployeeNumber();

    if (profileId != null && fileStorageService.exists(profileId)) {
      fileStorageService.delete(profileId);
    }

    employeeHistoryService.createEmployeeHistory(
        new EmployeeHistoryCreateRequest(HistoryType.DELETED, empNumber, List.of(), null),
        ipAddress
    );
  }

  @Override
  public List<EmployeeTrendDto> findEmployeeTrend(LocalDate startDate, LocalDate endDate,
      EmployeeTrendTimeUnit unit) {

    // 1. 기본값 및 기간 설정 (기존과 동일)
    LocalDate finalEnd = (endDate != null) ? endDate
        : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    EmployeeTrendTimeUnit finalUnit = (unit != null) ? unit : EmployeeTrendTimeUnit.MONTH;
    LocalDate finalStart;
    if (startDate != null) {
      finalStart = startDate;
    } else if (finalUnit == EmployeeTrendTimeUnit.YEAR) {
      finalStart = finalEnd.minusYears(4).withDayOfYear(1);
    } else {
      finalStart = finalEnd.minusMonths(11).withDayOfMonth(1);
    }

    // [수정] 2. 시작 시점(finalStart) 이전의 전체 직원 수를 먼저 가져옵니다.
    // 이 메서드는 repository에 별도로 구현되어야 합니다. (예: finalStart 이전 입사자 - 퇴사자)
    int totalCount = employeeRepository.countTotalEmployeesBefore(finalStart);

    // 3. 해당 기간 내의 "월별 증감 데이터" 조회
    // rawResults에는 각 월별로 '순수하게 늘거나 줄어든 인원수'가 들어있어야 합니다.
    List<EmployeeTrendMapping> rawResults = employeeRepository.getTrendData(
        finalStart, finalEnd, finalUnit.name()
    );

    Map<String, Integer> dbDataMap = rawResults.stream()
        .collect(Collectors.toMap(EmployeeTrendMapping::getPeriod, EmployeeTrendMapping::getCount));

    // 4. 전체 기간 루프 돌며 누적치 계산
    List<EmployeeTrendDto> result = new ArrayList<>();
    LocalDate current = finalStart;

    // 누적 계산을 위해 이전 달의 '총 인원'을 저장할 변수
    int previousTotal = totalCount;

    while (!current.isAfter(finalEnd)) {
      String dateKey = formatDate(current, finalUnit);

      // 해당 월의 순수 증감분 (예: +3명, -1명 등)
      int monthlyChange = dbDataMap.getOrDefault(dateKey, 0);

      // [핵심] 현재 총 인원 = 이전 총 인원 + 이번 달 증감분
      int currentTotal = previousTotal + monthlyChange;

      // 변동률 계산 (이미지에는 안 나오지만 기존 로직 유지 시)
      double changeRate = (previousTotal == 0) ? (currentTotal > 0 ? 100.0 : 0.0)
          : ((double) monthlyChange / previousTotal) * 100;

      result.add(new EmployeeTrendDto(
          dateKey,
          currentTotal, // 차트의 Y축 값이 될 누적 인원수
          monthlyChange,
          Math.round(changeRate * 100) / 100.0
      ));

      previousTotal = currentTotal; // 다음 루프를 위해 현재 총합을 저장
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
      endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
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