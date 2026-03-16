package com.team1.hrbank.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.dto.cursor.CursorPageResponseEmployeeHistoryDto;
import com.team1.hrbank.dto.request.EmployeeHistoryCreateRequest;
import com.team1.hrbank.entity.EmployeeHistory;
import com.team1.hrbank.entity.HistoryType;
import com.team1.hrbank.global.ResourceNotFoundException;
import com.team1.hrbank.mapper.EmployeeHistoryMapper;
import com.team1.hrbank.repository.EmployeeHistoryRepository;
import com.team1.hrbank.service.EmployeeHistoryService;
import java.time.Instant;
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
  private final EmployeeHistoryMapper employeeHistoryMapper; // Entity↔DTO 변환
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public void createEmployeeHistory(EmployeeHistoryCreateRequest request, String ipAddress) {

    // request.diffs()로 받은 List<DiffDto>를 JSON 문자열로 변환
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
  public List<EmployeeHistoryDto> findAllEmployeeHistories() {
    return employeeHistoryRepository.findAll()
        .stream()
        .map(employeeHistoryMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public List<EmployeeHistoryDto> findEmployeeHistoriesByRevisionsBetween(Long fromRevision,
      Long toRevision) {
    return employeeHistoryRepository.findAll()
        .stream()
        // id가 fromRevision 이상, toRevision 이하인 것만 필터링
        .filter(h -> h.getId() >= fromRevision && h.getId() <= toRevision)
        .map(employeeHistoryMapper::toDto) // Mapper로 변환
        .collect(Collectors.toList());
  }

  @Override
  public CursorPageResponseEmployeeHistoryDto findEmployeeHistories(String employeeNumber,
      HistoryType type, String memo, String ipAddress, Instant atFrom, Instant atTo, Long idAfter,
      String cursor, int size, String sortField, String sortDirection
  ) {
    Long cursorId = (cursor != null) ? Long.parseLong(cursor) : idAfter;
    // 1 조회
    List<EmployeeHistory> histories =
        employeeHistoryRepository.findHistoriesWithConditions(
            employeeNumber,
            memo,
            ipAddress,
            type,
            atFrom,
            atTo,
            cursorId
        );

    // 2 size + 1 만큼만 사용
    int limit = Math.min(histories.size(), size + 1);
    histories = histories.subList(0, limit);

    // 3 다음 페이지 존재 여부
    boolean hasNext = histories.size() > size;

    if (hasNext) {
      histories = histories.subList(0, size);
    }

    // 4 DTO 변환
    List<EmployeeHistoryDto> content = histories.stream()
        .map(employeeHistoryMapper::toDto)
        .toList();

    // 5 next cursor 계산
    String nextCursor = null;
    Long nextIdAfter = null;

    if (hasNext && !histories.isEmpty()) {
      EmployeeHistory last = histories.get(histories.size() - 1);
      nextCursor = last.getCreatedAt().toString();
      nextIdAfter = last.getId();
    }

    // 6 전체 개수 조회
    long total = employeeHistoryRepository.count();

    // 7 응답 생성
    return new CursorPageResponseEmployeeHistoryDto(
        content,
        nextCursor,
        nextIdAfter,
        size,
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
