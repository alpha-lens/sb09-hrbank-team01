package com.team1.hrbank.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.dto.request.EmployeeHistorySearchRequest;
import com.team1.hrbank.entity.EmployeeHistory;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.mapper.EmployeeHistoryMapper;
import com.team1.hrbank.repository.EmployeeHistoryRepository;
import com.team1.hrbank.service.EmployeeHistoryService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeHistoryServiceImpl implements EmployeeHistoryService {

  private final EmployeeHistoryRepository employeeHistoryRepository;
  private final EmployeeHistoryMapper employeeHistoryMapper;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void createEmployeeHistory(EmployeeHistoryCreateRequest request, String ipAddress) {

    String diffJson = serializeDiff(request.diffs());

    EmployeeHistory history = EmployeeHistory.of(
        request.type(),
        request.employeeNumber(),
        diffJson,
        request.memo(),
        ipAddress
    );

    employeeHistoryRepository.save(history);
  }

  @Override
  public EmployeeHistoryDetailDto findEmployeeHistory(Long id) {
    EmployeeHistory history = employeeHistoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("EmployeeHistory not found"));

    List<DiffDto> diffs = deserializeDiff(history.getDiffJson());

    return employeeHistoryMapper.toDetailDto(history,diffs);

  }

  @Override
  public long countEmployeeHistories(Instant fromDate, Instant toDate) {

    Instant from = fromDate != null ? fromDate : Instant.now().minus(7, ChronoUnit.DAYS);
    Instant to = toDate != null ? toDate : Instant.now();

    return employeeHistoryRepository.countByCreatedAtBetween(from, to);
  }

  @Override
  public CursorPageResponseEmployeeHistoryDto findEmployeeHistories(EmployeeHistorySearchRequest request)
  {
    Long cursorId = 0L;
    if (request.cursor() != null) {
      cursorId = request.cursor();
    } else if (request.idAfter() != null) {
      cursorId = request.idAfter();
    }

    String sortField = "at".equals(request.sortField()) ? "createdAt" : "ipAddress";

    // sortDirection: "asc" 또는 "desc"
    Sort.Direction direction = Sort.Direction.fromString(
        request.sortDirection() != null ? request.sortDirection() : "desc"
    );

    Pageable pageable = PageRequest.of(0, request.size() + 1,
        Sort.by(direction, sortField));

    // 1 조회
    List<EmployeeHistory> histories =
        employeeHistoryRepository.findHistoriesWithConditions(
            request.employeeNumber(),
            request.memo(),
            request.ipAddress(),
            request.historyType(),
            request.atFrom(),
            request.atTo(),
            cursorId,
            pageable
        );

    boolean hasNext = histories.size() > request.size();
    List<EmployeeHistory> content = hasNext
        ? histories.subList(0, request.size())
        : histories;

    // 다음 페이지 커서 = 현재 페이지 마지막 요소의 id
    Long nextCursor = null;
    Long nextIdAfter = null;
    if (hasNext && !content.isEmpty()) {
      EmployeeHistory last = content.get(content.size() - 1);
      nextCursor = last.getId();
      nextIdAfter = last.getId();
    }

    // DTO 변환
    List<EmployeeHistoryDto> dtoContent = content.stream()
        .map(employeeHistoryMapper::toDto)
        .toList();

    // 전체 건수
    long total = employeeHistoryRepository.countByConditions(
        request.employeeNumber(),
        request.memo(),
        request.ipAddress(),
        request.historyType(),
        request.atFrom(),
        request.atTo()
    );

    return new CursorPageResponseEmployeeHistoryDto(
        dtoContent,
        nextCursor,
        nextIdAfter,
        request.size(),
        total,
        hasNext
    );
  }

  // List<DiffDto> → JSON 문자열 (저장할 때 사용)
  private String serializeDiff(List<DiffDto> diffs) {
    if (diffs == null || diffs.isEmpty()) return null;
    try {
      return objectMapper.writeValueAsString(diffs);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("diff 직렬화 실패", e);
    }
  }

  // JSON 문자열 → List<DiffDto> (조회할 때 사용)
  private List<DiffDto> deserializeDiff(String diffJson) {
    if (diffJson == null) return List.of();
    try {
      return objectMapper.readValue(diffJson, new TypeReference<>() {});
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("diff 역직렬화 실패", e);
    }
  }
}
