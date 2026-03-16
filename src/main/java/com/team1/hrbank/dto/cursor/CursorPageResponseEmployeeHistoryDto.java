package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.EmployeeHistoryDto;
import java.util.List;

public record CursorPageResponseEmployeeHistoryDto (
    List<EmployeeHistoryDto> content,
    Long nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
