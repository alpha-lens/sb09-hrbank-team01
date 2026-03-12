package com.team1.hrbank.dto.cursor;

import com.team1.hrbank.dto.EmployeeDto;
import java.util.List;

public record CursorPageResponseEmployeeDto(
    List<EmployeeDto> content,
    String nextCursor,
    int nextIdAfter,
    int size,
    int totalElements,
    boolean hasNext
) {

}
