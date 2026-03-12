package com.team1.hrbank.service.impl;

import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.entity.EmployeeHistory;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.repository.EmployeeHistoryRepository;
import com.team1.hrbank.service.EmployeeHistoryService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeHistoryServiceImpl implements EmployeeHistoryService {

  private final EmployeeHistoryRepository employeeHistoryRepository;

  @Override
  public EmployeeHistoryDto createEmployeeHistory(EmployeeHistoryCreateRequest request) {

    EmployeeHistory history = EmployeeHistory.of(
        request.type(),
        request.employeeNumber(),
        request.diffJson(),
        request.memo(),
        request.ipAddress()
    );

    EmployeeHistory saved = employeeHistoryRepository.save(history);

    return toDto(saved);
  }

  @Override
  public EmployeeHistoryDto findEmployeeHistory(Long id) {
    EmployeeHistory history = employeeHistoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("EmployeeHistory not found"));

    return toDto(history);
  }

  @Override
  public List<EmployeeHistoryDto> findAllEmployeeHistories() {
    if (employeeHistoryRepository.count() == 0) {
      throw new ResourceNotFoundException("EmployeeHistory not found");
    }
    return employeeHistoryRepository.findAll()
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<EmployeeHistoryDto> findEmployeeHistoriesByRevisionsBetween(Long fromRevision,
      Long toRevision) {
    return employeeHistoryRepository
        .findByIdBetween(fromRevision, toRevision)
        .stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  private EmployeeHistoryDto toDto(EmployeeHistory history) {
    return new EmployeeHistoryDto(
        history.getId(),
        history.getType(),
        history.getEmployeeNumber(),
        history.getDiffJson(),
        history.getMemo(),
        history.getCreatedAt()
    );
  }
}
