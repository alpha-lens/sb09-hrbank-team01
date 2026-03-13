package com.team1.hrbank.mapper;

import com.team1.hrbank.dto.DiffDto;
import com.team1.hrbank.dto.EmployeeHistoryDetailDto;
import com.team1.hrbank.dto.EmployeeHistoryDto;
import com.team1.hrbank.entity.EmployeeHistory;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class EmployeeHistoryMapper {

  // 목록 조회용 (diffJson 제외)
  public EmployeeHistoryDto toDto(EmployeeHistory history) {
    return new EmployeeHistoryDto(
        history.getId(),
        history.getType(),
        history.getEmployeeNumber(),
        history.getMemo(),
        history.getIpAddress(),
        history.getCreatedAt()
    );
  }

  // 상세 조회용 (diffJson 포함)
  public EmployeeHistoryDetailDto toDetailDto(
      EmployeeHistory history,
      List<DiffDto> diffs   // 역직렬화된 diffs 받아서 넣기
  ) {
    return new EmployeeHistoryDetailDto(
        history.getId(),
        history.getType(),
        history.getEmployeeNumber(),
        history.getMemo(),
        history.getIpAddress(),
        history.getCreatedAt(),
        diffs
    );
  }
}